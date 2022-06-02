//发送验证码
function publish() {
	var email=$("#email").val();
	//发送异步请求
	$.post(
		CONTEXT_PATH+"/sendVerdify",
		{"email":email},
		function (data){
			data=$.parseJSON(data);//转为json对象
			if(data.emailMsg!=null){
				console.log(data.emailMsg);
				$("#email").attr('class','form-control is-invalid');
				$("#emailMsg").text(data.emailMsg);
			}else{
				$("#email").attr('class','form-control');
			}
			if(data.sendMsg!=null){
				$("#sendMsg").text(data.sendMsg);
				var btn=$("#sendVerdifyBtn");
				//倒数计时
				resendCpatcha(btn,60);
			}


		}
	);
	//倒数计时
	function resendCpatcha (btnEle, countdown){
		btnEle.text('已经发送' + '(' + countdown + ')');
		var t = setInterval(function() {
			if (--countdown === 0){
				//可以点击
				clearInterval(t);
				btnEle.attr('class','btn btn-info form-control');
				btnEle.text('重新获取验证码');
			} else{
				//不可以点击
				btnEle.attr('class','btn btn-info form-control disabled');
				btnEle.text('已经发送' + '(' + countdown + ')');
			}
		}, 1000);
	}


}