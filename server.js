var app = require('./app');

app.get('/api', function (req, res) {
    res.send('API is running');
});

var port = process.env.PORT || '8080';
app.listen(port, function () {
    console.log('My Application Running on http://localhost:'+port+'/');
});