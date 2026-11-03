from pathlib import Path
import json,subprocess,sys,datetime
root=Path(__file__).resolve().parent
out=Path('G:/emoney-bio-test/bio-reports')
out.mkdir(exist_ok=True)
results=[]
for name in ['bio_dump','bio_machine_probe','bio_gt_output_probe','bio_smelting_run','bio_recipe_run','bio_bootstrap_probe']:
    call=subprocess.run([sys.executable,'-I',str(root/'bio_bridge.py'),'exec',str(root/(name+'.snippet'))],capture_output=True,encoding='utf-8',timeout=120)
    try: response=json.loads(call.stdout)
    except ValueError: response={'ok':False,'error':call.stderr or call.stdout}
    text=response.get('result',{}).get('result','') if isinstance(response.get('result'),dict) else ''
    passed=response.get('ok') and (text.startswith('PASS') or text.startswith('DUMP_OK'))
    results.append({'name':name,'passed':bool(passed),'response':response})
    print(name, 'PASS' if passed else 'FAIL',text or response.get('error'),flush=True)
(out/'machine-probes.json').write_text(json.dumps({'time':datetime.datetime.now().isoformat(),'results':results},ensure_ascii=False,indent=2),encoding='utf-8')
if results[0]['passed']:
    check=subprocess.run([sys.executable,'-I',str(root/'verify_bio_dump.py')],capture_output=True,encoding='utf-8')
    (out/'assertions.txt').write_text(check.stdout+check.stderr,encoding='utf-8')
    print(check.stdout.splitlines()[-1] if check.stdout else check.stderr)
    if check.returncode: raise SystemExit(1)
raise SystemExit(0 if all(r['passed'] for r in results) else 1)
