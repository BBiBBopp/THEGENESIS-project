<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<!-- jQuery 라이브러리 -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
<!-- 부트스트랩에서 제공하고 있는 스타일 -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
<!-- 부트스트랩에서 제공하고 있는 스크립트 -->
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
<link rel="stylesheet" href="resources/css/chat.css" />
</head>
<body>

	<button class="connect-button" onclick="connect()" hidden>접속</button>
	
	<script>
		$(function(){
			// 채팅 들어오면 자동 소켓 연결
			$(".connect-button").trigger('click');
		})
	
		var socket;		
		
		// 웹소켓 접속 함수
		function connect() {
			var uri = "ws://localhost:8706/sweethome/groupchat"
			socket = new WebSocket(uri);
			
			// 연결이 되었는지 안되었는지 확인할 수 있도록 예약작업(콜백)을 설정
			socket.onopen = function() {
				console.log("서버와 연결되었습니다.");
			}
			socket.onclose = function() {
				console.log("서버와 연결이 종료되었습니다.");
			}
			socket.onerror = function(e) {
				console.log("오타 내지마세요.");
			}
			socket.onmessage = function(result) { // 메시지를 받으면 동작
				var result = result.data;
				if(result != null && result.trim() != ""){ // 메시지가 비어있지 않을 때
					var m = JSON.parse(result);
					if(m.type == "message") { // 메시지 보여주기
						if(m.userId == "${loginUser.userId}") {
							var outgoing = $("<div class='outgoing_msg'></div>");
							var sent = $("<div class='sent_msg'></div>");
							
							sent.html("<p>" + m.msg + "</p> <span class='time_date'>" + m.nowTime + "</span>");
							outgoing.append(sent);
							
							$(".msg_history").append(outgoing);
						} else {
							var income = $("<div class='incoming_msg'></div>");
							var received = $("<div class='received_msg'></div>");
							var msg = $("<div class='received_withd_msg'></div>");
							
							msg.html("<p>" + m.msg + "</p> <span class='time_date'>" + m.nowTime + "</span>");
							received.append(msg);
							income.append(received);
							
							$(".msg_history").append(income);			
						}
					}
				}

				// 스크롤 맨 아래로
				$(".msg_history").scrollTop($(".msg_history")[0].scrollHeight);
			}
		}
		
		// 웹소켓 종료함수
		function disconnect() {
			socket.close();
		}
		
		// 메시지 전송 함수
		function send() {
			var text = $(".write_msg").val();
			// 텍스트 없으면 안보냄
			if(!text) {
				alert("메시지를 입력해주세요");
				return;
			}
			// 메시지에 들어가는 정보
			// 메시지 타입, userId, 메시지 내용
			var sendInfo = {
					type: "message",
					userId: "${loginUser.userId}",
					msg: text
			}
			
			// 소켓에 전송
			socket.send(JSON.stringify(sendInfo));
			// 메세지 전송 부분 비우기
			$(".write_msg").val("");
		}
		
		// 엔터키로 메시지 보내기
		function msgSend() {
			if(event.keyCode == 13)			
			$(".msg_send_btn").trigger("click");
		}
	</script>
	<!-- 
	<hr>
	<input type="text" id="chat-input"> 
	<button onclick="send()">전송</button>
	 -->
	<!-- 수신된 메시지가 출력될 영역 -->
	<div class="messaging">
	  <div class="inbox_msg">
		<div class="inbox_people">
		  <div class="headind_srch">
			<div class="recent_heading">
			  <h4>Recent</h4>
			</div>
			<div class="srch_bar">
              <div class="stylish-input-group">
                <input type="text" class="search-bar"  placeholder="Search" >
                <span class="input-group-addon">
                <button type="button">🔍</button>
                </span> </div>
            </div>
		  </div>
		  <div class="inbox_chat scroll">
			<div class="chat_list active_chat">
			  <div class="chat_people">
				<div class="chat_img"> <img src="https://ptetutorials.com/images/user-profile.png" alt="sunil"> </div>
				<div class="chat_ib">
				  <h5>Sunil Rajput <span class="chat_date">Dec 25</span></h5>
				  <p>Test, which is a new approach to have all solutions 
					astrology under one roof.</p>
				</div>
			  </div>
			</div>
			<c:if test="${ empty chatList }">
				<div style="margin:50px auto auto auto; text-align: center;">
					<img src="resources/image/Nochat.png" style="width:50px;"><br>
					<b>
					생성된 대화가 없습니다.<br>
					새로운 대화를 시작해보세요! 
					</b>
				</div>
			</c:if>
			<c:forEach var="c" items="${ chatList }">
				<div class="chat_list" onclick="goChat();">
				  <div class="chat_people">
					<div class="chat_img"> <img src="https://ptetutorials.com/images/user-profile.png" alt="sunil"> </div>
					<div class="chat_ib">
					  <h5><b>${ c.roomName }</b> <span class="chat_date">${ c.sendTime }</span></h5>
					  <p>
					  <c:choose>
					  <c:when test="${ fn:length(c.message) lt 20 }">
						  ${ c.message }
					  </c:when>
					  <c:otherwise>
					  	${ fn:substring(c.message, 0, 20) }...
					  </c:otherwise>
					  </c:choose>
					  </p>
					</div>
				  </div>
				</div>
			</c:forEach>
		  </div>
		</div>
		<div class="mesgs">
		  <div class="msg_history">
		  <div class="hr-sect">2022년 03월 23일</div>
		  </div>
		  <div class="type_msg">
			<div class="input_msg_write">
			  <input type="text" class="write_msg" placeholder="Type a message" onkeypress="msgSend();"/>
			  <button class="msg_send_btn" type="button" onclick="send()"><img src="resources/image/Daco_4358108.png" style="width: 100%;"></button>
			</div>
		  </div>
		</div>
	  </div>
	</div>

	
	
</body>
</html>