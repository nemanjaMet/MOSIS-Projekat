var express = require('express');
var app = express();
var http = require('http').createServer(app);
var io = require('socket.io')(http);

var bodyParser = require('body-parser');
app.use(bodyParser.json({limit: '5mb'}));
app.use(bodyParser.urlencoded({limit: '5mb', extended: true}));
var fs = require("fs");
var path = require("path");

app.use(express.static(__dirname + '/node_modules'));
app.get('/', function (req, res, next) {
    //res.sendFile(__dirname + '/index.html');
});

// Baza SQLite
var sqlite3 = require('sqlite3').verbose();
var db = new sqlite3.Database('projekatDB.db');
var check;
db.serialize(function () {

    db.run("CREATE TABLE if not exists 'User' (" +
        "'ID'	INTEGER PRIMARY KEY AUTOINCREMENT," +
        "'Username'	TEXT NOT NULL UNIQUE," +
        "'Password'	TEXT NOT NULL," +
        "'Name' TEXT," +
        "'LastName' TEXT," +
        "'PhoneNumber' TEXT," +
        "'Image' TEXT" +
        //"'Image' BLOB" +
    ");");

});

db.close();

// Create application/x-www-form-urlencoded parser
var urlencodedParser = bodyParser.urlencoded({ extended: false })

app.post('/process_newuser', urlencodedParser, function (req, res) {
    console.log("Novi podaci...");
    // Prepare output in JSON format
    response = {
        username: req.body.username,
        password: req.body.password,
        name: req.body.name,
        lastname: req.body.lastname,
        phonenumber: req.body.phonenumber,
        image: req.body.image
    };
    
  
        var db = new sqlite3.Database('projekatDB.db');
        db.serialize(function () {
            var query = db.prepare("INSERT into User(Username,Password, Name, LastName, PhoneNumber, Image) VALUES ('" + response.username + "','" + response.password + "','" + response.name + "','" + response.lastname + "','" + response.phonenumber +  "','" + response.image + "')");
        /*db.run("INSERT into User(Username,Password, Name, LastName, PhoneNumber) VALUES ('" + response.username + "','" + response.password + "','" + response.name + "','" + response.lastname + "','" + response.phonenumber + "')", new function(err) {
             if (err) {
                console.log(err.message);
                res.send("userErr");
                return;
        }
        }
            );*/
        query.finalize();
        
        });
        db.close();

        console.log("---SEND---");
        console.log(response);
        //res.end(JSON.stringify(response));
        //res.end("complete");
        res.send("success");
        /*res.format({
        'text/plain': function () {
            res.send('complete');
        }
        });*/   
   
    
});

app.post('/process_checkuser', urlencodedParser, function (req, res) {
    console.log("Got a GET request for /process_checkuser");
    //res.send('Page Pattern Match');
    console.log("Provera usera...");
    // Prepare output in JSON format
    var response = {
        username: req.body.username,
        password: req.body.password
    };

   // console.log("Username: " + response.username + "Password: " + response.password);
 
    var db = new sqlite3.Database('projekatDB.db');
    db.serialize(function () {
        //db.all("SELECT * from User where Username='" + response.username + "' and Password='" + response.password + "'", function (err, rows) {
            db.all("SELECT Username, Password from User where Username='" + response.username + "'", function (err, rows) {

            //rows contain values while errors, well you can figure out.
            // res.send(JSON.stringify(rows));

            var broj = 0;
            rows.forEach(function (row) {
                broj++;
            });

            //console.log(rows);
           // console.log(broj);
           /* var test = JSON.stringify(rows);
            console.log("Test: " + test);*/
            if (broj == '1') {
                //res.send("correct");
                var user;
                rows.forEach(function (row) {
                    user = row;//JSON.parse(String(row));
            });
                console.log(user);
                console.log(user.Password + " == " + response.password);
                if (user.Password == response.password)
                    res.send("correct");
                else
                    res.send("passwErr")
            }
            else
                res.send("usernErr");
        });
        db.close();
    });
    


});

var server = http.listen(process.env.PORT || 8081, function () {

    var host = 'localhost'; //server.address().address;
    var port = process.env.PORT; //server.address().port;

    console.log("Example app listening at http://%s:%s", host, port);

});


