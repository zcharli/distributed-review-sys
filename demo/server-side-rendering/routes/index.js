var express = require('express');
var router = express.Router();
var http = require('http');

var productBarcode = "861123656411";
var url = "192.168.101.17";
var port = 9090;

/* GET home page. */
router.get('/', function (req, res, next) {
    res.redirect('/nick-the-messenger-bag-17l');
});
// {
//     "review_content": "Review of Toy",
//     "stars": 5,
//     "upvotes": 1,
//     "barcode": "28221",
//     "title": "Generic Toyr",
//     "description": "Toy is ok",
//     "type": "commodity"
// }
router.get('/nick-the-messenger-bag-17l', function (req, res, next) {
    var path = "/api/review/get/" + productBarcode;

    var options = {
        host: url,
        port: port,
        path: path,
        method: 'GET',
    };
    var response = res;
    var req = http.request(options, function (res) {
        res.setEncoding('utf8');
        res.on('data', function (chunk) {
            console.log(`BODY: ${chunk}`);
            var results = false;
            chunk = JSON.parse(chunk);
            if (chunk.status === 200) {
                results = chunk.results;
                console.log(results);
                results.map((item) => {
                    item.published_time = new Date(item.publish_time).toLocaleTimeString("en-us", {
                        weekday: "long", year: "numeric", month: "short", timezone: "America/New_York",
                        day: "numeric", hour: "2-digit", minute: "2-digit"
                    });
                    console.log(item.publish_time);
                    item.totalVotes = (parseInt(item.upvotes) || 0) + (parseInt(item.downvotes) || 0);
                    item.starWidth = parseInt(item.upvotes) / 5;

                    return item;
                });
            } else {
                console.log(chunk);
            }
            response.render('index', {
                reviews: results,
            });
        });
        res.on('end', () => {
            console.log('No more data in response.');
        });
    });

    req.on('error', (e) => {
        console.log(`problem with request: ${e.message}`);
    });
    req.end();

});

router.post("/review/new", function(req, res, next) {
    var postParams = req.body;
    console.log(postParams);
    var postData = {
        review_content: postParams.detail,
        stars: postParams.stars,
        upvotes: 0,
        barcode: productBarcode,
        title: postParams.title,
        description: postParams.name,
        type: "commodity"
    };

    var path = "/api/review/new/" + encodeURIComponent(postParams.name);
    postData = JSON.stringify(postData);
    var options = {
        host: url,
        port: port,
        path: path,
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': Buffer.byteLength(postData)
        }
    };

    console.log(options);
    var response = res;
    var req = http.request(options, function (res) {
        console.log('STATUS: ' + res.statusCode);
        console.log('HEADERS: ' + JSON.stringify(res.headers));
        res.setEncoding('utf8');
        res.on('data', function (chunk) {
            console.log(`BODY: ${chunk}`);
            var results = false;
            chunk = JSON.parse(chunk);
            if (chunk.status === 200) {
                response.send("success");
            } else {
                response.send(chunk.responseText);
            }
        });
        res.on('end', () => {
            console.log('No more data in response.');
        });
    });

    req.on('error', (e) => {
        console.log(`problem with request: ${e.message}`);
    });
    req.write(postData);
    req.end();
});

router.post("/review/vote", function(req, res, next) {
    var postParams = req.body;
    console.log(postParams);

    if (!postParams.hasOwnProperty("vote")) {
        res.send({status: 400, responseText: "missing vote param"});
    }

    var votePath = postParams.vote ? "upvote" : "downvote";
    var path = "/api/review/"+votePath+"/"+postParams.locationId + "/" + postParams.contentId;
    console.log(path);
    var options = {
        host: url,
        port: port,
        path: path,
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        }
    };
    console.log(options);
    var response = res;
    var req = http.request(options, function (res) {
        res.setEncoding('utf8');
        res.on('data', function (chunk) {
            console.log("fuck");
            var results = false;
            console.log(chunk);
            chunk = JSON.parse(chunk);
            console.log(chunk);
            if (chunk.status === 200 || chunk.message === "Success") {
                response.send({status: 200});
            } else {
                response.send(chunk.responseText);
            }
        });
        res.on('end', () => {
            console.log('No more data in response.');
        });
    });

    req.on('error', (e) => {
        console.log(`problem with request: ${e.message}`);
    });
    req.end();

});

module.exports = router;
