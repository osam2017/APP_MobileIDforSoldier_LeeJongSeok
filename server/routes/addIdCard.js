/**
 * 출입증을 추가하는 라우팅 함수. public 폴더의 addIdCard.html에서 받은 출입증 추가 요청을 처리.
 */

var pool;

// db pool 초기화 메소드, app.js에서 호출
var init = function(parampool) {
    pool = parampool;
}

// 출입증을 추가하는 메소드
var add = function(ServiceNumber, rank, name, IssueDate, callback) {
    // 커넥션 풀에서 연결 객체 가져오기
    pool.getConnection(function(err, conn) {
        if (err) {
            if (conn) {
                conn.release();
            }

            callback(err, null);
            return;
        }

        // 데이터를 객체로 만들기
        var data = {
            servicenumber: ServiceNumber,
            rank: rank,
            name: name,
            date: IssueDate
        };

        // INSERT SQL문 실행
        var tableName = 'LeeJongSeok_idcard';
        var exec = conn.query('INSERT INTO ' + tableName + ' SET ?', data, function(err, result) {
            conn.release();
            console.log('신규 출입증 저장 중...');

            if (err) { // 쿼리에서 에러 반환 시
                console.log('Error recurred.');
                console.dir(err);

                callback(err, null);

                return;
            }

            callback(null, result); // 쿼리 성공 시, 결과를 반한
        });
    });
};

// 출입증을 추가하기 위한 라우팅 메소드
var addIdCard = function(req, res) {
    console.log('출입증 추가 메소드 호출됨.');

    var paramServiceNumber = req.body.ServiceNumber || req.query.ServiceNumber;
    var paramRank = req.body.Rank || req.query.Rank;
    var paramName = req.body.Name || req.query.Name;
    var paramDate = req.body.Date || req.query.Date;

    if (pool) { // db 연결 시, 출입증 데이터 추가 시도
        add(paramServiceNumber, paramRank, paramName, paramDate, function(err, added) {
            // error 발생 시
            if (err) {
                console.log('출입증 추가 중 오류 발생 : ' + err.stack);

                res.writeHead('200', {
                    'Content-Type': 'text/html;charset=utf8'
                });
                res.write('<h2>출입증 추가 중 오류 발생</h2>');
                res.end();

                return;
            }

            // 성공 시, 성공 응답 전송
            if (added) {
                console.log('군번 : ' + paramServiceNumber + '인 출입증 추가됨.');

                res.writeHead('200', {
                    'Content-Type': 'text/html;charset=utf8'
                });
                res.write('<h2>출입증 추가 완료</h2>');
                res.write('<p>군번 : ' + paramServiceNumber +
                    '<br> 계급 : ' + paramRank +
                    '<br> 이름 : ' + paramName +
                    '<br> 발급일자 : ' + paramDate +
                    '<br>   로 추가됨.</p>');
                res.end();
            } else {
                res.writeHead('200', {
                    'Content-Type': 'text/html;charset=utf8'
                });
                res.write('<h2>출입증 추가 실패</h2>');
                res.end();
            }
        });
    } else { // db 초기화 실패 시
        res.writeHead('200', {
            'Content-Type': 'text/html;charset=utf8'
        });
        res.write('<h2>데이터베이스 연결 실패</h2>');
        res.end();
    }
}

module.exports.init = init;
module.exports.addIdCard = addIdCard;