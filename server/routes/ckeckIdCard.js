/**
 * 출입증을 조회하는 라우팅 함수. android app에서 /confirmcard로 요청한 조회 시도를 처리한 후
 * 응답을 조회를 시도한 app으로 반환.
 */

var pool;

// db pool 초기화 메소드, app.js에서 호출
var init = function(parampool) {
    pool = parampool;
}

// 출입증을 조회하는 메소드
var query = function(ServiceNumber, Rank, Name, Date, callback) {
    // 커넥션 풀에서 연결 객체 가져오기
    pool.getConnection(function(err, conn) {
        if (err) {
            if (conn) {
                conn.release();
            }

            callback(err, null);
            return;
        }

        var tableName = 'LeeJongSeok_idcard';
        var columns = ['servicenumber', 'rank', 'name', 'date'];

        // SELECT SQL문 실행
        var selectSQL = "SELECT ?? FROM ?? WHERE servicenumber = ? AND rank = ? AND name = ? AND date = ?";
        var exec = conn.query(selectSQL, [columns, tableName, ServiceNumber, Rank, Name, Date], function(err, result) {
            conn.release();
            console.log('군번 : ' + ServiceNumber + '\n계급 : ' + Rank + '\n이름 : ' + Name + 
                        '\n발급일자 : ' + Date + '\n 인 출입증 조회 시도함...');

            if (err) {
                console.log('Error recurred.');
                console.dir(err);

                callback(err, null);

                return;
            }

            callback(null, result);
        });
    });
};

// 앱에서 보내온 출입증 데이터를 서버의 것과 비교하여 있으면 OK를 앱으로 다시 쏴주는 메소드
var checkIdCard = function(req, res) {
    console.log('출입증 확인 함수 호출됨.');

    var paramServiceNumber = req.body.ServiceNumber || req.query.ServiceNumber;
    var paramRank = req.body.Rank || req.query.Rank;
    var paramName = req.body.Name || req.query.Name;
    var paramDate = req.body.Date || req.query.Date;

    if (pool) {
        query(paramServiceNumber, paramRank, paramName, paramDate, function(err, found) {
            // error 발생 시
            if (err) {
                console.log('출입증 쿼리 중 에러 발생...');

                res.writeHead('200', {
                    'Content-type': 'text/plain;charset=utf8'
                });
                res.write("message: 데이터베이스 조회 실패...");
                res.end();
                return;
            }

            // 성공 시, 성공 응답 전송
            if (found) {
                console.log('등록된 출입증 발견됨.');

                res.writeHead('200', {
                    'Content-type': 'text/plain;charset=utf8'
                });
                res.write("message: 출입증 정품 확인 성공.");
                res.end();

            } else {
                console.log('기기에서 승인 요청한 출입증이 서버에 등록되지 않았음.');

                res.writeHead('200', {
                    'Content-type': 'text/plain;charset=utf8'
                });
                res.write("message: 출입증이 등록되지 않았음.");
                res.end();
            }
        });
    } else { // db 초기화 실패 시
        console.log('데이터베이스 연결 실패...');

        res.writeHead('200', {
            'Content-type': 'text/plain;charset=utf8'
        });
        res.write("error: '서버 연결 실패...");
        res.end();
    }
};

module.exports.init = init;
module.exports.checkIdCard = checkIdCard;