from pathlib import Path
import json

root=Path('G:/emoney-bio-test/bio-reports')
d=json.loads((root/'runtime-recipes.json').read_text(encoding='utf-8'))
rows={r['id']:r for r in d['recipes']}
checks=[]
def check(ok, message):
    checks.append({'pass':bool(ok),'check':message})
    print(('PASS ' if ok else 'FAIL ')+message)
def recipe(part):
    found=[r for key,r in rows.items() if part in key and r['type'] != 'gtceu:large_chemical_reactor']
    check(len(found)==1,part+' unique recipe')
    return found[0] if found else {}
meat=recipe('bio/primitive_meat_to_meat_dust')
check(meat.get('duration')==102,'meat duration 102')
ips=recipe('bio/ips_brain_bootstrap')
check(ips.get('duration')==1600,'bootstrap duration 1600')
stem=recipe('bio/ips_assisted_stem_cells')
check(stem.get('duration')==240,'assisted cells duration 240')
for r in [ips,stem]:
    text=json.dumps(r)
    check('sterile' in text and '30720' in text, r.get('id','?')+' LuV sterile condition')
check('32' in json.dumps(stem.get('outputs',{})) and 'bacterial_sludge' in json.dumps(stem.get('outputs',{})),'stem output and sludge preserved')
check(any('stem_cells' in k and 'bio/' not in k for k in rows),'original stem recipe remains')
check(any('raw_growth_medium' in k for k in rows),'original raw growth medium remains')
check(any('sterile_growth_medium' in k for k in rows),'original sterile medium remains')
for element in ['air','fire','water','earth','order','entropy']:
    for suffix in ['_dust','_gem']:
        key='pollution:infused_'+element+suffix
        check(sum(d['elementAspects'].get(key,{}).values())==1,key+' exactly one essentia')
    r=recipe('element_crystal_reverse/'+element)
    output=json.dumps(r.get('outputs',{}))
    check('crystal_cluster' not in output and 'infused_'+element in output,'fluid reverse outputs own gem '+element)
allowed={'iron','copper','tin','lead','silver','gold','zinc'}
check(bool(d['orePigDrops']),'ore pig nonempty pool')
check(all(x['material'] in allowed and 'nugget' in x['item'] for x in d['orePigDrops']),'ore pig only whitelisted nuggets')
(root/'assertions.json').write_text(json.dumps(checks,indent=2),encoding='utf-8')
failed=sum(not c['pass'] for c in checks)
print('TOTAL',len(checks),'FAILED',failed)
raise SystemExit(bool(failed))
