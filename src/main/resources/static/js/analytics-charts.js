function parseOrMock(id, mock) {
  try {
    var raw = document.getElementById(id).textContent.trim();
    if (!raw || raw === '{}' || raw === 'null') return mock;
    var p = JSON.parse(raw);
    return Object.keys(p).length === 0 ? mock : p;
  } catch(e) { return mock; }
}
var today = new Date();
function daysAgo(n) { var d=new Date(today); d.setDate(d.getDate()-n); return d.toISOString().split('T')[0]; }
var mA={},mR={};
for(var i=29;i>=0;i--){mA[daysAgo(i)]=Math.floor(Math.random()*10)+1;mR[daysAgo(i)]=Math.floor(Math.random()*3);}
var mU={'Dr. Rajesh':12,'Dr. Priya':8,'Dr. Arun':15,'Dr. Kavitha':10};
var mS={'PENDING':5,'CONFIRMED':12,'COMPLETED':20,'CANCELLED':3};
var mL={'Approved Leaves':4,'Cancellations from Leave':7};

var apptD=parseOrMock('d-appt',mA), docD=parseOrMock('d-util',mU),
    statD=parseOrMock('d-status',mS), patD=parseOrMock('d-reg',mR), leaveD=parseOrMock('d-leave',mL);

new Chart(document.getElementById('apptLine'),{type:'line',data:{labels:Object.keys(apptD),datasets:[{label:'Appointments',data:Object.values(apptD),borderColor:'#1a73e8',backgroundColor:'rgba(26,115,232,0.1)',fill:true,tension:0.4,pointRadius:3}]},options:{plugins:{legend:{display:false}},scales:{y:{beginAtZero:true,ticks:{stepSize:1}}}}});
var sc={PENDING:'#ff9800',CONFIRMED:'#4caf50',COMPLETED:'#2196f3',CANCELLED:'#9e9e9e',REJECTED:'#f44336'};
new Chart(document.getElementById('statusDonut'),{type:'doughnut',data:{labels:Object.keys(statD),datasets:[{data:Object.values(statD),backgroundColor:Object.keys(statD).map(function(k){return sc[k]||'#607d8b';}),borderWidth:0}]},options:{plugins:{legend:{position:'bottom'}}}});
new Chart(document.getElementById('doctorBar'),{type:'bar',data:{labels:Object.keys(docD),datasets:[{label:'Appointments',data:Object.values(docD),backgroundColor:['#4caf50','#1a73e8','#ff9800','#9c27b0','#f44336','#00bcd4'],borderRadius:8}]},options:{plugins:{legend:{display:false}},scales:{y:{beginAtZero:true,ticks:{stepSize:1}}}}});
new Chart(document.getElementById('patientLine'),{type:'line',data:{labels:Object.keys(patD),datasets:[{label:'New Patients',data:Object.values(patD),borderColor:'#00897b',backgroundColor:'rgba(0,137,123,0.1)',fill:true,tension:0.4,pointRadius:3}]},options:{plugins:{legend:{display:false}},scales:{y:{beginAtZero:true,ticks:{stepSize:1}}}}});
new Chart(document.getElementById('leaveBar'),{type:'bar',data:{labels:Object.keys(leaveD),datasets:[{data:Object.values(leaveD),backgroundColor:['#f57c00','#e53935'],borderRadius:8}]},options:{plugins:{legend:{display:false}},scales:{y:{beginAtZero:true,ticks:{stepSize:1}}}}});
