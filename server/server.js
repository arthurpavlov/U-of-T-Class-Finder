var express = require('express');
var mongoose = require('mongoose');
var fs = require('fs');
var bodyParser = require('body-parser');
var request = require('request');

mongoose.connect('mongodb://localhost/buildings');
mongoose.connect('mongodb://localhost/users');

let Buildings = require('./model/buildings');
let Users = require('./model/users');

fs.readFile('buildings.json', 'utf8', function (err, data) {
	if (err) throw err;
	var json = JSON.parse(data);
	Buildings.collection.insert(json.buildings, function(err, doc) {
		if(err) throw err;
	});
});
// fs.readFile('test_users.json', 'utf8', function (err, data) {
//     if (err) throw err;
//     var json = JSON.parse(data);
//     Users.collection.insert(json.test_users, function(err, doc) {
//         if(err) throw err;
//     });
// });


var app = express();
app.use(bodyParser.json());
app.use(express.static(__dirname));
app.engine('.html', require('ejs').__express);
app.set('views', __dirname);
app.set('view engine', 'html');
app.use(bodyParser.urlencoded({
    extended: true
}));


//TO DO:
//1)POST ROUTE FOR USER CREATION (/createuser)
//2)POST ROUTE CHECK IF USER EXISTS (/users/:username)
//3)POST ROUTE CHECK IF USER CALENDAR EXISTS (/usercalendars/:username)
//4)GET ROUTE FOR GETTING USER CALENDAR FILE (/users/:username)
//5)PUT ROUTE FOR UPDATING USER CALENDAR FILE (/usercalendars/:username)


app.get('/buildings/:code', function(req, res){
	var building_code = req.params.code;
	Buildings.find({code:building_code}, function(err, bs){
		res.send(bs);
	});
})

//USER CREATION
// test command
// curl -v -X POST http://localhost:3000/createuser -H Content-Type:application/json -d "{\"email\":\"arthurp\",\"password\":\"123123\"}"
app.post('/createuser', function(req,  res){
    var givenParsed = JSON.parse(JSON.stringify(req.body));
    var givenEmail = givenParsed.email;
    var givenPassword = givenParsed.password;
    Users.find({}).exec(function(err, AllUsers){
        if (err) throw err;
        //check if user exists
        var userFound = false;
        outUsers = JSON.stringify(AllUsers);
        for (i = 0; i < AllUsers.length; i++) { 
            var user = JSON.stringify(AllUsers[i]);
            var json = JSON.parse(user);
            if  (json.email == givenEmail){
                userFound = true;   
            }
        }
        //User doesn't exist, add to db
        if(!userFound){
            res.json({success: true});
            var response = {
            email:givenEmail,
            password:givenPassword,
            calendar:''
            };
            new Users(response).save(function (err, newuser) {
                if (err) {
                    throw err;
                } else {
                    console.log("User Created");
                }
            });
        }
        //user exists
        else{
            res.json({success: false});
            console.log("User already Exists!")
        }
    });
})

//CHECK IF USER EXISTS
// test command
// curl -v -X POST http://localhost:3000/users -H Content-Type:application/json -d "{\"email\":\"arthurp\",\"password\":\"123123\"}"
app.post('/users', function(req, res){
    var givenParsed = JSON.parse(JSON.stringify(req.body));
    var givenEmail = givenParsed.email;
    var givenPassword = givenParsed.password;
    Users.find({}).exec(function(err, AllUsers){
        if (err) throw err;
        //check if user exists
        var userFound = false;
        outUsers = JSON.stringify(AllUsers);
        for (i = 0; i < AllUsers.length; i++) { 
            var user = JSON.stringify(AllUsers[i]);
            var json = JSON.parse(user);
            if  (json.email == givenEmail && json.password == givenPassword){
                userFound = true;   
            }
        }
        res.json({success: userFound});
    });
})

//CHECK IF USER CALENDAR EXISTS
// test command
// curl -v -X POST http://localhost:3000/usercalendars/arthurp 
app.post('/usercalendars/:email', function(req, res){
    var givenEmail = req.params.email;
    Users.find({email:givenEmail}).exec(function(err, UserInfo){
        if (err) throw err;
        if(UserInfo.length != 0){
            var userCalendar = JSON.parse(JSON.stringify(UserInfo[0])).calendar;
            if(userCalendar == ''){
                res.json({success:false});
            }
            else{
                res.json({success:true});
            }
        }
        else{
            res.json({error:true});
        }
    });
    
})

//GET USER CALENDAR FILE
// test command
// curl -v -X GET http://localhost:3000/users/arthurp
app.get('/users/:email', function(req, res){
    var givenEmail = req.params.email;
    Users.find({email:givenEmail}).exec(function(err, UserInfo){
        if (err) throw err;
        if(UserInfo.length != 0){
            var userCalendar = JSON.parse(JSON.stringify(UserInfo[0])).calendar;
            res.json({success:userCalendar});
        }
        else{
            res.json({error:true});
        }
    });
})

//UPDATE USER CALENDAR FILE
// test command
// curl -v -X PUT http://localhost:3000/usercalendars/arthurp -H Content-Type:application/json -d "{\"calendar\":\"calendar_file_1\"}"
app.put('/usercalendars/:email', function(req, res){
    var givenEmail = req.params.email;
    var givenParsed = JSON.parse(JSON.stringify(req.body));
    var givenCalendar = givenParsed.calendar;
    Users.update({email:givenEmail},{calendar:givenCalendar}).exec(function(err, AllUsers){
        if (err) throw err;
        if(AllUsers.length != 0){
            res.json({success:true});
        }
        else{
            res.json({error:true});
        }
    });
})







// Google API handling

var googleKey = "AIzaSyAHGoj5ig9YkXqjxL7-9wiN8abbCwDsy8k";
var googleApiUrl = "https://maps.google.com/maps/api/";

app.post("/route", function(req, res){
	var bd = req.body;
    var response_object = {};
    var response_key = "response";
    response_object[response_key] = [];
    
    for (var index in req.body.paths) {
        var pathObj = req.body.paths[index];
        var origin = pathObj.origin.replace(/\s/g, "+");
        var destination = pathObj.destination.replace(/\s/g, "+");
        var arrival_time = pathObj.desired_arrival_time;
        var mode = pathObj.mode;
        
        var url = googleApiUrl + "directions/json?" + "mode=" + mode + "&origin=" + origin + "&destination=" + destination + "&arrival_time=" + arrival_time + "&key=" + googleKey;
        
        request(url, function (error, response, body) {
            var bodyObj = JSON.parse(body);
            response_object[response_key].push(bodyObj);
            if (response_object[response_key].length == req.body.paths.length) {
                res.send(response_object);
            }
        });
    }
});

app.post("/geocoding", function(req, res){
    var bd = req.body;
    var addr = req.body.address;
    var url = googleApiUrl + "geocode/json?address=" + addr + "&key=" + googleKey;
    
    request(url, function (error, response, body) {
        var bodyObj = JSON.parse(body)
        res.send(bodyObj);
    });
});

app.listen(process.env.PORT || 3000);
console.log('Listening on port 80');