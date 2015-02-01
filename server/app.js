// var express = require('express');
// var app = require('express.io')()
// app.http().io()
// app.listen(4000)
var express = require('express');
var fs = require('fs');
var app = express();
var server = app.listen(4000);
var io = require('socket.io').listen(server);
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');


var mongo = require('mongoskin');
var db = mongo.db("mongodb://localhost:27017/myapp" , {native_parser:true});

var routes = require('./routes/index');
var users = require('./routes/users');

// var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

// uncomment after placing your favicon in /public
//app.use(favicon(__dirname + '/public/favicon.ico'));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
 app.use(express.static(path.join(__dirname, 'public')));

app.use(function(req,res,next){
  req.db = db;
  next();
});
app.get('/', function(req, res, next) {
  res.send('Send Apt query please :(');
});

io.on('connection', function(socket){
  socket.on('connectUser', function(msg){
    var userid = msg;
    // console.log(userid);
  //db.collection('userlist').find({id: {$in: 'test1'}});
  db.collection('userlist').find({id:userid},{username:1,_id:0}).toArray(function(err,items){
   if(!items[0]) 
   {
    // console.log("blablabla");
    db.collection('userlist').insert(
    {
     username: socket.id,
     id: userid,
     level:1,
     status:1,
     match:null 
   }
   ,function(err,result){
    if(err) throw err;
    // if(result)
      // console.log('insertes');
  })
  }
  else
  {
    // console.log("blablabla");
    db.collection('userlist').update(
      {   id:userid },
      {   $set:{status:1,username:socket.id}},
      {multi:true},
      function(err,result){
        if(err) throw err;
        // if(result) 
          // console.log('updated');
      }
    )

  }
      //io.emit("connected",items[0].username);  
    });
});
});



io.on('connection',function(socket){
    socket.on('questions',function(msg){
      console.log("User connected:\033[92m",socket.id,'\033[0m');
      var obj;
      fs.readFile('./1.json', 'utf8', function (err, data) {
        if (err) throw err;
        obj = JSON.parse(data);

      socket.send(data);
        //console.log(obj);
      });
    });
});

io.on('connection',function(socket){
    socket.on('questions2',function(msg){
      console.log("blabla");
      var obj;
      fs.readFile('./2.json', 'utf8', function (err, data) {
        if (err) throw err;
        obj = JSON.parse(data);

      socket.send(data);
        //console.log(obj);
      });
    });
});



io.on('connection',function(socket){
  socket.on('disconnect', function() {

    db.collection('userlist').find({username:socket.id}).toArray(function (err,items) {
      try{socket.to(items[0].match).emit('userDisconnected','yep he did')}catch(ex){}
      // console.log('hoo hoo',items[0].match);
    })

    db.collection('userlist').update(
      {   username:socket.id },
      {   $set:{status:0,username:null,match:null}},
      {multi:true},
      function(err,result){
        if(err) throw err;
        // if(result) 
          // console.log('reset');
      }
    )


  });

io.on('connection',function(socket){
socket.on('answerPressed',function(msg){
    var id = msg.id;
    var totalxp = msg.xp;
    //var level = msg.level;
    
        db.collection('userlist').find({username:socket.id}).toArray(function (err,items) {
      try{
        var oppo = items[0].match;
      //   if(oppoxp - totalxp > 0)
      //       socket.emit('oppowinning',(oppoxp - totalxp));
      //   else
      //       socket.emit('userwinning',(totalxp - oppoxp));   
      // }catch(ex){}
      //console.log(oppo);
      socket.to(oppo).emit('oppoxp',totalxp);
      // console.log('hoo hoo',items[0].match);
    }catch(e){}
  })

});
});






  socket.on('matchUser',function(msg){
    // console.log(socket.id);
    // console.log(msg.userid);
    var userid = msg.userid;
    var userlevel = msg.level;
    // socket.join('room'+id);
    db.collection('userlist').find({level:userlevel,match :null,id:{$ne:userid},username:{$ne:null}}).toArray(function(err,items){
      // console.log(items)
      if (items.length>0) {
        // console.log('found some elements');
        socket.emit('receiveMatchingUser',items[0].id);
        socket.to(items[0].username).emit('receiveMatchingUser',userid)
        db.collection('userlist').update(
          {   id:userid },
          {   $set:{match:items[0].username}},
          {multi:true},
          function(err,result){
            if(err) throw err;
            if(result) 
            {
              // console.log('userMatched');
              // socket.join('match');
              // io.sockets.in('match').emit('matchstarted','begin');
            }
          }
        )
        db.collection('userlist').update(
          {   id:items[0].id },
          {   $set:{match:socket.id}},
          {multi:true},
          function(err,result){
            if(err) throw err;
            if(result) 
            {
              // console.log('userMatched');
              // socket.join('match');
              // io.sockets.in('match').emit('matchstarted','begin');
            }
          }
        )
      }
    });
  });
});











app.get('/connect/:id', function(req, res, next) {
  var userid = req.params.id;
  // console.log(userid);
  var db = req.db;
  //db.collection('userlist').find({id: {$in: 'test1'}});
  db.collection('userlist').find({id:userid},{username:1,_id:0}).toArray(function(err,items){
    res.send('match',items[0].username);  
  });
});
app.get('/username/:level/questions',function(req,res,next){
  //var
});

app.use('/users', users);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
  app.use(function(err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
      message: err.message,
      error: err
    });
  });
}

// production error handler
// no stacktraces leaked to user
app.use(function(err, req, res, next) {
  res.status(err.status || 500);
  res.render('error', {
    message: err.message,
    error: {}
  });
});


//dule.exports = app;
