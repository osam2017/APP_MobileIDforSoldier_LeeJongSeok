// 기본 모듈 불러오기
var express = require('express');
var http = require('http');
var path = require('path');

// express의 미들웨어 모듈 불러오기
var bodyParser = require('body-parser');
var static = require('serve-static');

// mysql모듈 불러오기
var mysql = require('mysql');

// MySQL database의 커넥션 풀 설정
var pool = mysql.createPool({
    connectionLimit: 100,
    host: 'localhost',
    user: 'root',
    port : 3306,
    password: 'root',
    database: 'OSAM'
});

// addIdCard 메소드 할당
var addIdCard = require('./routes/addIdCard');
addIdCard.init(pool);

// checkIdCard 메소드 할당
var checkIdCard = require('./routes/ckeckIdCard');
checkIdCard.init(pool);

// 익스프레스 객체 생성
var app = express();

app.set('port', process.env.PORT || 5037);

// application/x-www-form-urlencoded 파싱
app.use(bodyParser.urlencoded({
    extended: false
}));
// application.json 파싱
app.use(bodyParser.json());

// public폴더에 대한 접근 허용
app.use('/public', static(path.join(__dirname, 'public')));

// 라우터 객체 참조
var router = express.Router();

// 웹으로 서버에 접속 시, 출입증 추가 페이지로 리다이렉트
router.route('/').get(function(req, res){
	res.redirect('/public/addidcard.html');
});

// 출입증 추가 라우팅 함수 등록 (html에서 요청)
router.route('/addidcard').post(addIdCard.addIdCard);

// 출입증 확인 라우팅 함수 등록 (app에서 요청)
router.route('/confirmcard').post(checkIdCard.checkIdCard);

// 라우터 객체를 app객체에 등록
app.use('/', router);

// ----- Express 서버 시작 ----- //
http.createServer(app).listen(app.get('port'), function() {
    console.log('Server started at : ' + app.get('port'));
});