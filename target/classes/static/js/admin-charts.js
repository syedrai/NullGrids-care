function parseOrMock(id, mockData) {
  try {
    var raw = document.getElementById(id).textContent.trim();
    if (!raw || raw === '{}' || raw === 'null') return mockData;
    var parsed = JSON.parse(raw);
    if (Object.keys(parsed).length === 0) return mockData;
    return parsed;
  } catch(e) { return mockData; }
}

var today = new Date();
function daysAgo(n) {
  var d = new Date(today); d.setDate(d.getDate() - n);
  return d.toISOString().split('T')[0];
}

var mockAppt = {};
mockAppt[daysAgo(6)]=3; mockAppt[daysAgo(5)]=5; mockAppt[daysAgo(4)]=2;
mockAppt[daysAgo(3)]=7; mockAppt[daysAgo(2)]=4; mockAppt[daysAgo(1)]=6; mockAppt[daysAgo(0)]=8;

var mockUtil   = {'Dr. Rajesh':12,'Dr. Priya':8,'Dr. Arun':15,'Dr. Kavitha':10,'Dr. Suresh':6,'Dr. Anitha':9};
var mockStatus = {'PENDING':5,'CONFIRMED':12,'COMPLETED':20,'CANCELLED':3};
var mockReg    = {};
mockReg[daysAgo(6)]=2; mockReg[daysAgo(5)]=1; mockReg[daysAgo(4)]=3;
mockReg[daysAgo(3)]=0; mockReg[daysAgo(2)]=2; mockReg[daysAgo(1)]=4; mockReg[daysAgo(0)]=1;
var mockLeave  = {'Approved Leaves':4,'Cancellations from Leave':7};

var apptD  = parseOrMock('d-appt',   mockAppt);
var docD   = parseOrMock('d-util',   mockUtil);
var statD  = parseOrMock('d-status', mockStatus);
var patD   = parseOrMock('d-reg',    mockReg);
var leaveD = parseOrMock('d-leave',  mockLeave);

new Chart(document.getElementById('apptLineChart'), {
  type: 'line',
  data: {
    labels: Object.keys(apptD),
    datasets: [{ label: 'Appointments', data: Object.values(apptD),
      borderColor: '#1a73e8', backgroundColor: 'rgba(26,115,232,0.1)',
      fill: true, tension: 0.4, pointBackgroundColor: '#1a73e8', pointRadius: 5 }]
  },
  options: { plugins:{legend:{display:false}}, scales:{y:{beginAtZero:true,ticks:{stepSize:1}}} }
});

var statusColors = {PENDING:'#ff9800',CONFIRMED:'#4caf50',COMPLETED:'#2196f3',CANCELLED:'#9e9e9e',REJECTED:'#f44336'};
new Chart(document.getElementById('statusDonut'), {
  type: 'doughnut',
  data: {
    labels: Object.keys(statD),
    datasets: [{ data: Object.values(statD),
      backgroundColor: Object.keys(statD).map(function(k){ return statusColors[k]||'#607d8b'; }),
      borderWidth: 0 }]
  },
  options: { plugins:{legend:{position:'bottom'}} }
});

new Chart(document.getElementById('doctorBar'), {
  type: 'bar',
  data: {
    labels: Object.keys(docD),
    datasets: [{ label: 'Appointments', data: Object.values(docD),
      backgroundColor: ['#4caf50','#1a73e8','#ff9800','#9c27b0','#f44336','#00bcd4'],
      borderRadius: 8 }]
  },
  options: { plugins:{legend:{display:false}}, scales:{y:{beginAtZero:true,ticks:{stepSize:1}}} }
});

new Chart(document.getElementById('patientLine'), {
  type: 'line',
  data: {
    labels: Object.keys(patD),
    datasets: [{ label: 'New Patients', data: Object.values(patD),
      borderColor: '#00897b', backgroundColor: 'rgba(0,137,123,0.1)',
      fill: true, tension: 0.4, pointBackgroundColor: '#00897b', pointRadius: 5 }]
  },
  options: { plugins:{legend:{display:false}}, scales:{y:{beginAtZero:true,ticks:{stepSize:1}}} }
});

new Chart(document.getElementById('leaveBar'), {
  type: 'bar',
  data: {
    labels: Object.keys(leaveD),
    datasets: [{ data: Object.values(leaveD),
      backgroundColor: ['#f57c00','#e53935'], borderRadius: 8 }]
  },
  options: { plugins:{legend:{display:false}}, scales:{y:{beginAtZero:true,ticks:{stepSize:1}}} }
});
