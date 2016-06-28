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
        "'Lastname' TEXT," +
        "'PhoneNumber' TEXT," +
        "'Image' TEXT," +
        "'Created' TEXT" +
        //"'Image' BLOB" +
    ");");

    db.run("CREATE TABLE if not exists 'Friendship' (" +
        "'ID'   INTEGER PRIMARY KEY AUTOINCREMENT," +
        "'Team_name' TEXT NOT NULL UNIQUE," +
        "'User1' TEXT NOT NULL," +
        "'User2' TEXT NOT NULL" +
    ");");

    db.run("CREATE TABLE if not exists 'Questions' (" +
        "'ID'   INTEGER PRIMARY KEY AUTOINCREMENT," +
        "'Question' TEXT NOT NULL," +
        "'CorrectAnswer' TEXT NOT NULL," +
        "'WrongAnswer' TEXT NOT NULL," +
        "'Category' TEXT NOT NULL," +
        //"'CategoryLongLat' TEXT NOT NULL," +
        "'LongitudeCategory' TEXT NOT NULL," +
        "'LatitudeCategory' TEXT NOT NULL," +
        "'CreatedUser' TEXT NOT NULL," +
         "'UsersTryToAnswer' TEXT," +
        "'LongitudeLatitude' TEXT NOT NULL" +
    ");");

});

db.close();

// Create application/x-www-form-urlencoded parser
var urlencodedParser = bodyParser.urlencoded({ extended: false })

app.post('/process_newquestion', urlencodedParser, function (req, res) {
    console.log("New Question...");
    // Prepare output in JSON format
    response = {
        category: req.body.category,
        questions: req.body.questions,
        correctAnswers: req.body.correctAnswers,
        wrongAnswers: req.body.wrongAnswers,
        longitudeLatitude: req.body.longitudeLatitude,
        //categoryLongLat: req.body.categoryLongLat,
        categoryLong: req.body.categoryLong,
        categoryLat: req.body.categoryLat,
        createdUser: req.body.createdUser,
        friendsUsernames: req.body.friendsUsernames
    };
        var usersBlocked = "," + response.createdUser + ",";
        if (response.friendsUsernames.length > 0)
        {
            usersBlocked += response.friendsUsernames + ",";
        }

        console.log("Blocked friends: " + usersBlocked);
  
        var db = new sqlite3.Database('projekatDB.db');
        db.serialize(function () {
           // var query = db.prepare("INSERT into User(Username,Password, Name, LastName, PhoneNumber, Image) VALUES ('" + response.username + "','" + response.password + "','" + response.name + "','" + response.lastname + "','" + response.phonenumber +  "','" + response.image + "')");
        db.run("INSERT into Questions(Category,Question, CorrectAnswer, WrongAnswer, LongitudeLatitude, LongitudeCategory, LatitudeCategory, UsersTryToAnswer, CreatedUser) VALUES ('" + response.category + "','" + response.questions + "','" + response.correctAnswers + "','" + response.wrongAnswers + "','" + response.longitudeLatitude +  "','" + response.categoryLong + "','" + response.categoryLat + "','" + usersBlocked +"','" + response.createdUser + "')", new function(err) {
             if (err) {
                console.log(err.message);
                res.send("questErr");
                return;
        }
        }
            );
        //query.finalize();
        
        });
        db.close();

        console.log("---SEND Question---");
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

app.post('/process_getCategory', urlencodedParser, function (req, res) {

    console.log("Get category...");
    // Prepare output in JSON format
    var response = {
        myLong: req.body.myLong,
        myLat: req.body.myLat,
        myUsername: req.body.myUsername,
        friendsUsernames: req.body.friendsUsernames
        //questionID: req.body.questionID
    };

    var splited = response.friendsUsernames.split(",");
    var query = "SELECT ID,Category,LongitudeCategory,LatitudeCategory,CreatedUser from Questions where ";
    for (var i = 0; i < splited.length; i++)
    {
        query += "UsersTryToAnswer NOT LIKE '%," + splited[i] + ",%' AND ";
    }
    //query = query.substring(3,query.lastIndexOf("AND"));
    query += "UsersTryToAnswer NOT LIKE '%," + response.myUsername + ",%'";
    console.log("Query quest: " + query);

    var db = new sqlite3.Database('projekatDB.db');
    db.serialize(function () {
        //db.all("SELECT * from User where Username='" + response.username + "' and Password='" + response.password + "'", function (err, rows) {
            db.all(query, function (err, rows) {

            //rows contain values while errors, well you can figure out.
            // res.send(JSON.stringify(rows));
            console.log("Result category: " + JSON.stringify(rows));

            if (err)
                res.send("error");
            res.send(JSON.stringify(rows));

        });
        db.close();
    });

});

app.post('/process_getQuestion', urlencodedParser, function (req, res) {

    console.log("Get questions...");
    // Prepare output in JSON format
    var response = {
        questID: req.body.questID
    };

    var db = new sqlite3.Database('projekatDB.db');
    db.serialize(function () {
        //db.all("SELECT * from User where Username='" + response.username + "' and Password='" + response.password + "'", function (err, rows) {
            db.all("SELECT ID,Questions,CorrectAnswer,WrongAnswer,LongitudeLatitude from Questions where ID '" + response.questID, function (err, rows) {

            //rows contain values while errors, well you can figure out.
            // res.send(JSON.stringify(rows));
            console.log("Result category: " + JSON.stringify(rows));

            if (err)
                res.send(err);
            res.send(JSON.stringify(rows));

        });
        db.close();
    });

});

app.post('/process_newuser', urlencodedParser, function (req, res) {
    console.log("Novi podaci...");
    // Prepare output in JSON format
    response = {
        username: req.body.username,
        password: req.body.password,
        name: req.body.name,
        lastname: req.body.lastname,
        phonenumber: req.body.phonenumber,
        image: req.body.image,
        created: req.body.created
    };
    
  
        var db = new sqlite3.Database('projekatDB.db');
        db.serialize(function () {
           // var query = db.prepare("INSERT into User(Username,Password, Name, LastName, PhoneNumber, Image) VALUES ('" + response.username + "','" + response.password + "','" + response.name + "','" + response.lastname + "','" + response.phonenumber +  "','" + response.image + "')");
        db.run("INSERT into User(Username,Password, Name, LastName, PhoneNumber, Image, Created) VALUES ('" + response.username + "','" + response.password + "','" + response.name + "','" + response.lastname + "','" + response.phonenumber +  "','" + response.image + "','" + response.created + "')", new function(err) {
             if (err) {
                console.log(err.message);
                res.send("userErr");
                return;
        }
        }
            );
        //query.finalize();
        
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

app.post('/process_getFriendProfile', urlencodedParser, function (req, res) {

    console.log("Get friend profile...");
    // Prepare output in JSON format
    var response = {
        username: req.body.username
    };
 
    var db = new sqlite3.Database('projekatDB.db');
    db.serialize(function () {
        //db.all("SELECT * from User where Username='" + response.username + "' and Password='" + response.password + "'", function (err, rows) {
            db.all("SELECT Username,Name,Lastname,PhoneNumber,Image,Created from User where Username='" + response.username + "'", function (err, rows) {

            //rows contain values while errors, well you can figure out.
            // res.send(JSON.stringify(rows));

            if (err)
                res.send(err);
            res.send(JSON.stringify(rows));

        });
        db.close();
    });

});

app.post('/process_getfriendship', urlencodedParser, function (req, res) {
    console.log("Get friendships...");
    // Prepare output in JSON format
    response = {
       // team_name: req.body.team_name,
        username: req.body.username,
    };
    
    console.log(response);
  
        var db = new sqlite3.Database('projekatDB.db');
        var query = "SELECT Username, Created from User where";
        db.serialize(function () {
           // var query = db.prepare("INSERT into User(Username,Password, Name, LastName, PhoneNumber, Image) VALUES ('" + response.username + "','" + response.password + "','" + response.name + "','" + response.lastname + "','" + response.phonenumber +  "','" + response.image + "')");
        db.all("SELECT * from Friendship where User1='" + response.username +"' OR User2='" + response.username + "'" , function (err, rows) {
            //res.send(rows);
            /*var usernames = "";
            rows.forEach(function (row) {
                if (row.User1 == response.username)
                    usernames += row.User2 + ",";
                else
                    usernames += row.User1 + ",";
            });
            usernames = usernames.substring(1,usernames.lastIndexOf(","));
            res.send(usernames);*/
            
            //var userCreated = [];
            if (err)
                return;

            var broj = 0;
            rows.forEach(function (row) {
                query += " Username='";
                if (row.User1 == response.username)
                    query += row.User2;
                else
                    query += row.User1;
                query += "' OR"
                broj++;
                 /*db.all("SELECT Username, Created from User where Username='" + response.username + "'", function (err2, rows2) {
                    //var user = {Username: rows2[0].Username, Created: rows2[0].Created};
                    rows2.forEach(function (row2) {
                        var user = {Username: row2.Username, Created: row2.Created};
                    })
                    userCreated.push(user);
                 });*/
            });
            //query = query.substring(0, query.length-2); // BEZ SOPSTVENOG PROFILA
            query += " Username='" + response.username + "'";
            console.log(query);
            if (broj == 0)     
                query = "noFriends";    
            getFriends(query, function(result) {
                console.log("Result from function: " + result);
                res.send(result);
            });
            /*console.log("Result from function: " + result);
            res.send(result);  */
        });
             
        db.close();
    });   
        
        /*var db = new sqlite3.Database('projekatDB.db');
        db.serialize(function () {
        if (query != "noFriends")
        {
            
            db.all(query, function (err, rows) {
                console.log(JSON.stringify(rows))
                     res.send(JSON.stringify(rows));
                });
            
        }
        else
        {
            res.send("noFriends");
        }
        });
        db.close();*/

});

function getFriends(query, callback){
    var db = new sqlite3.Database('projekatDB.db');
        db.serialize(function () {
        if (query != "noFriends")
        {
            
            db.all(query, function (err, rows) {
                //console.log(JSON.stringify(rows))
                     callback(JSON.stringify(rows)) ;
                });
            
        }
        else
        {
            //return "noFriends";
            callback("noFriends")
        }
        });
        db.close();
}


app.post('/process_newfriendship', urlencodedParser, function (req, res) {
    console.log("Novi podaci...");
    // Prepare output in JSON format
    response = {
        team_name: req.body.team_name,
        username1: req.body.username1,
        username2: req.body.username2
    };
    
  
        var db = new sqlite3.Database('projekatDB.db');
        db.serialize(function () {
           // var query = db.prepare("INSERT into User(Username,Password, Name, LastName, PhoneNumber, Image) VALUES ('" + response.username + "','" + response.password + "','" + response.name + "','" + response.lastname + "','" + response.phonenumber +  "','" + response.image + "')");
        db.run("INSERT into Friendship(Team_name,User1,User2) VALUES ('" + response.team_name + "','" + response.username1 + "','" + response.username2 + "')", new function(err) {
             if (err) {
                console.log(err.message);
                res.send("teamErr");
                return;
        }
        }
            );
        //query.finalize();
        
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

var usersLocations = [];
//var usersLocations = new Map();
// update-uje se soptsvena lokacija, i uzimaju se lokacije prijatelja
app.post('/process_updatelocation', urlencodedParser, function (req, res) {
    console.log("Update location...");
    // Prepare output in JSON format
    response = {
        username: req.body.username,
        friends: req.body.friends, // salje se username prijatelja
        longitude: req.body.longitude,
        latitude: req.body.latitude
    };

    console.log(response);

    var userloc = {
        Username: response.username,
        Longitude: response.longitude,
        Latitude: response.latitude
    }

    var friendsloc = [];
    var userExist = false;
    if (usersLocations.length > 0)
    {
        for (var i = 0; i < usersLocations.length; i++) {
            if (usersLocations[i].Username == userloc.Username) {
                 usersLocations[i].Longitude = userloc.Longitude;
                 usersLocations[i].Latitude = userloc.Latitude;
                 userExist = true;
            } else if (response.friends.includes(usersLocations[i].Username))
            {
                friendsloc.push(usersLocations[i]);
                console.log("Push: " + JSON.stringify(usersLocations[i]));
            }
        }
        if (!userExist)
            usersLocations.push(userloc);
    }
    else
    {
        usersLocations.push(userloc);
    }

    console.log(JSON.stringify(usersLocations));

    if (friendsloc.length > 0)
    { 
        //res.send(JSON.stringify(friendsloc));
        console.log("Friends loc sending: " + JSON.stringify(friendsloc));
        res.send(friendsloc);
    }
    else
    {
        console.log("No friends sending: nofriends");
        res.send("noFriends");
    }
});


var server = http.listen(process.env.PORT || 8081, function () {

    var host = 'localhost'; //server.address().address;
    var port = process.env.PORT; //server.address().port;

    console.log("Example app listening at http://%s:%s", host, port);

});


