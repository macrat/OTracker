package jp.blanktar.otracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import java.io.File
import kotlin.concurrent.timer


class Count(label: String, count: Int) {
    val label = label
    val count = count

    override fun toString() = "\"${label}\": ${count}"
}


class Counter(label: String, button: Button, view: TextView) {
    val label = label
    val button = button
    val view = view

    var count = 0
        set(value) {
            view.setText(value.toString())
            field = value
        }

    val result
        get() = Count(label, count)

    init {
        button.setOnClickListener {
            count++
        }
    }

    fun reset() {
        count = 0
    }
}


class Timer() {
    val start = System.currentTimeMillis()
    var end: Long? = null

    val duration
        get() = System.currentTimeMillis() - start

    override fun toString() = "%d:%02d".format(duration / 60 / 1000, duration / 1000 % 60)

    fun stop() {
        end = System.currentTimeMillis()
    }
}


class TrackData(label: String, counts: List<Count>, timer: Timer) {
    val label = label
    val counts = counts
    val timer = timer

    override fun toString() = """{"label": "${label}", "counts": {${counts.joinToString()}}, "time": {"start": ${timer.start / 1000}, "end": ${timer.end!! / 1000}}}"""
}


class Group(label: String, button: ToggleButton) {
    val label = label
    val button = button

    var counts: List<Count>? = null
    var timer: Timer? = null

    val running
        get() = timer != null

    init {
        val h = Handler()
        timer(initialDelay = 500, period = 500) {
            h.post {
                if (running) {
                    button.setTextOn(timer.toString())
                    button.setChecked(true)  // re-draw
                }
            }
        }
    }

    fun start(counts: List<Count>) {
        this.counts = counts
        this.timer = Timer()

        button.setChecked(true)
    }

    fun stop(): TrackData {
        timer!!.stop()

        button.setChecked(false)

        val result = TrackData(label, counts!!, timer!!)
        counts = null
        timer = null
        return result
    }
}


class tracker : AppCompatActivity() {
    var counters: List<Counter>? = null
    var groups: List<Group>? = null

    val counts
        get() = counters!!.map { x -> x.result }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker)

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        counters = listOf(Counter("m13", findViewById(R.id.m13) as Button, findViewById(R.id.m13v) as TextView),
                          Counter("f13", findViewById(R.id.f13) as Button, findViewById(R.id.f13v) as TextView),
                          Counter("m19", findViewById(R.id.m19) as Button, findViewById(R.id.m19v) as TextView),
                          Counter("f19", findViewById(R.id.f19) as Button, findViewById(R.id.f19v) as TextView),
                          Counter("m29", findViewById(R.id.m29) as Button, findViewById(R.id.m29v) as TextView),
                          Counter("f29", findViewById(R.id.f29) as Button, findViewById(R.id.f29v) as TextView),
                          Counter("m49", findViewById(R.id.m49) as Button, findViewById(R.id.m49v) as TextView),
                          Counter("f49", findViewById(R.id.f49) as Button, findViewById(R.id.f49v) as TextView),
                          Counter("m50", findViewById(R.id.m50) as Button, findViewById(R.id.m50v) as TextView),
                          Counter("f50", findViewById(R.id.f50) as Button, findViewById(R.id.f50v) as TextView))

        groups = listOf(Group("A", findViewById(R.id.group_a) as ToggleButton),
                        Group("B", findViewById(R.id.group_b) as ToggleButton),
                        Group("C", findViewById(R.id.group_c) as ToggleButton))

        for (g in groups!!) {
            g.button.setOnClickListener {
                if (g.running) {
                    appendTrack(g.stop())
                } else if (counts.map{ x -> x.count }.sum() > 0) {
                    g.start(counts)
                    resetCounters()
                } else {
                    g.button.setChecked(false)
                }
            }
        }

        (findViewById(R.id.clear) as Button).setOnClickListener {
            resetCounters()
        }
    }

    private fun appendTrack(data: TrackData) {
        File("/sdcard/otracker.txt").absoluteFile.appendText("${data}\n")
    }

    private fun resetCounters() {
        for (x in counters!!) {
            x.reset()
        }
    }
}
