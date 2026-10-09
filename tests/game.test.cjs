// Functional tests against production game code using a lightweight DOM/canvas harness.
const fs=require('fs'),vm=require('vm'),assert=require('assert');
const {createCanvas}=require('@napi-rs/canvas');
let store={},elements={},downloads=[];
function element(sel){if(elements[sel])return elements[sel];let c=sel.includes('world')?createCanvas(384,352):createCanvas(90,110),classes=new Set(sel==='#modal'?['hidden']:[]);return elements[sel]={innerHTML:'',textContent:'',value:'',style:{},classList:{add(x){classes.add(x)},remove(x){classes.delete(x)},contains(x){return classes.has(x)},toggle(x,v){if(v)classes.add(x);else classes.delete(x)}},getContext(){return c.getContext('2d')},width:c.width,height:c.height,addEventListener(){},click(){},getBoundingClientRect(){return {left:0,top:0,width:384,height:352}},scrollTop:0}}
const ctx={console,Date,Math,JSON,Blob,URL,setTimeout:()=>1,clearTimeout(){},requestAnimationFrame:()=>1,cancelAnimationFrame(){},performance:{now:()=>123},document:{querySelector:element,addEventListener(){},createElement(){return element('temp')}},localStorage:{getItem(k){return store[k]||null},setItem(k,v){store[k]=v}},kontra:{init(){},GameLoop(){return {start(){},stop(){}}}},window:{scrollTo(){},Native:{exportFile(name,data,type){downloads.push({name,data,type})}}}};
ctx.Native=ctx.window.Native;vm.createContext(ctx);
function run(code){return vm.runInContext(code,ctx)}
for(let f of ['content.js','game.js'])run(fs.readFileSync(__dirname+'/../app/src/main/assets/'+f,'utf8'));
run("$('#name').value='테스트 조사관';startGame();closeModal()");
assert.equal(run('state.name'),'테스트 조사관');
assert.equal(run('unlocked(1)'),false);
run("begin(3)");assert.equal(run('state.battle'),null);
run('accept(0); useSkill("scan")');assert.equal(run('state.battle.mana'),4);assert.equal(run('state.battle.selected.length'),1);
run('state.battle=null');
for(let i=0;i<13;i++){
 run(`begin(${i});accept(${i});selectEvidence(0);selectEvidence(1);evaluateEvidence();choose(0);advance();choose(0);advance()`);
 assert.equal(run('state.battle.phase'),'result',`quest ${i} reaches result`);
 assert.equal(run('state.battle.hp'),0);
 run('completeQuest();closeModal()');
 assert.equal(run(`!!state.done[${i}]`),true);
 assert.equal(run(`regionUnlocked(${Math.min(5,Math.floor((i+1)/2))})`),true);
}
assert.equal(run('count()'),13);assert.equal(run('state.xp'),1300);assert.equal(run('state.gold'),785);assert.equal(run('state.points'),26);assert.equal(run('state.skillPoints'),13);
const snapshot=run('JSON.stringify({xp:state.xp,gold:state.gold,stats:state.stats})');
run('accept(0);selectEvidence(0);selectEvidence(1);evaluateEvidence();choose(0);advance();choose(0);advance();completeQuest();closeModal()');
assert.equal(run('JSON.stringify({xp:state.xp,gold:state.gold,stats:state.stats})'),snapshot,'replay cannot farm rewards');
run("learn('guard');learn('focus');item('shield');item('seal')");assert.equal(run('state.equip["防具"]'),undefined);assert.equal(run('state.equip["방어구"]'),'shield');assert.equal(run('stat(2)'),run('state.stats[2]+3'));
run('accept(1);selectEvidence(0);selectEvidence(1);evaluateEvidence();useSkill("guard");useSkill("focus");choose(1)');assert.equal(run('state.battle.trust'),95);assert.equal(run('state.battle.mistakes'),1);
run('choose(1)');assert.equal(run('state.battle.mistakes'),1,'blocked option cannot deduct twice');
run('choose(0)');assert.equal(run('state.battle.boost'),false);
run('advance();choose(0);advance();completeQuest();closeModal()');
run('decideEvent(0,0);closeModal()');const eventGold=run('state.gold');run('decideEvent(0,0)');assert.equal(run('state.gold'),eventGold,'event reward cannot repeat');assert.equal(run('state.rep[0]'),78);
run('accept(0)');assert.equal(run('state.battle.budget'),110,'faction benefit');
run('state.battle.budget=10;state.battle.phase="strategy";state.battle.feedback=null;choose(1)');assert.equal(run('state.battle.phase'),'failed');run('retry()');assert.equal(run('state.battle.phase'),'dossier');
run('state.battle=null;save();go("world");move(0,-1);interact();go("hero");go("bag");go("codex");report();exportReport()');assert.equal(downloads.length,1);assert(downloads[0].data.includes('세 시장의 의뢰'));
assert.equal(run('validSave(JSON.parse(localStorage.getItem(SAVE)))'),true);
run('receiveSave("bad json")');assert.equal(run('state.name'),'테스트 조사관');
run('receiveSave(JSON.stringify(state));$("#restoreBtn").onclick()');assert.equal(run('state.battle'),null);
run('state=fresh();state.started=true;accept(0);selectEvidence(0);selectEvidence(2);evaluateEvidence()');assert.equal(run('state.battle.evidence'),false);assert.equal(run('state.battle.trust'),90);assert.equal(run('state.battle.days'),11);
run('state.battle=null;state.name="<img>";go("home")');assert(element('#hud').innerHTML.includes('&lt;img&gt;'));
console.log('PASS: 13-quest campaign, unlocks, replay reward guard, stat/equipment/skill effects, evidence/strategy costs, failure/retry, faction events, save/restore, report export, screen rendering.');
