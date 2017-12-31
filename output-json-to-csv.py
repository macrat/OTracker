import datetime
import json
import sys


print('label, start, end, duration, m13, f13, m19, f19, m29, f29, m49, f49, m50, f50')
with open(sys.argv[1], 'r') as f:
    for l in f:
        d = json.loads(l)
        s = datetime.datetime.fromtimestamp(d['time']['start'])
        e = datetime.datetime.fromtimestamp(d['time']['end'])
        d = (
            d['label'],
            s.isoformat(),
            e.isoformat(),
            (e - s).total_seconds() / 60.0,
            d['counts']['m13'],
            d['counts']['f13'],
            d['counts']['m19'],
            d['counts']['m19'],
            d['counts']['m29'],
            d['counts']['f29'],
            d['counts']['m49'],
            d['counts']['f49'],
            d['counts']['m50'],
            d['counts']['f50']
        )
        print(', '.join(map(str, d)))
