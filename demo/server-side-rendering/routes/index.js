var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
  res.redirect('/nick-the-messenger-bag-17l' + string);
});

router.get('nick-the-messenger-bag-17l', function(req, res, next) {


  res.render('index', { });
});

module.exports = router;
