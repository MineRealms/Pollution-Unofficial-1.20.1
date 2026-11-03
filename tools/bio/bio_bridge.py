"""Loopback-only Rosetta client for G:/emoney-bio-test. Never reads production credentials."""
from pathlib import Path
import json,socket,sys
run=Path('G:/emoney-bio-test')
token=(run/'RosettaRemoteDebugBridge/remote-token.txt').read_text().strip()
command=sys.argv[1] if len(sys.argv)>1 else 'ping'
request={'token':token,'cmd':command}
if command=='exec': request['args']={'code':Path(sys.argv[2]).read_text(encoding='utf-8')}
if command=='console': request['args']={'command':' '.join(sys.argv[2:])}
with socket.create_connection(('127.0.0.1',48790),timeout=60) as sock:
    sock.settimeout(90)
    stream=sock.makefile('rwb')
    stream.write((json.dumps(request)+'\n').encode());stream.flush()
    result=json.loads(stream.readline())
sys.stdout.reconfigure(encoding='utf-8')
print(json.dumps(result,ensure_ascii=False))
