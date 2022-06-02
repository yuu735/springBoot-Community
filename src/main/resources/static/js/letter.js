$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");//弹出框
	var toName=$("#recipient-name").val();
	var content=$("#message-text").val();
	$.post(
		CONTEXT_PATH+"/letter/send",
		{"toName":toName,"content":content},
		function (data){
			data=$.parseJSON(data);
			if(data.code==0){
				$("#hintBody").text("发送成功！");
			}else{
				$("#hintBody").text(data.msg);
			}
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();	//重载当前页面
			}, 2000);
		}
	);


}

function delete_msg() {
	// TODO 删除数据
	var id=$("#letterId").val();
	console.log(id);
	$.post(
		CONTEXT_PATH+"/letter/delete",
		{"id":id},
		function (data){
			data=$.parseJSON(data);
			if(data.code==0){
				$("#hintBody").text("删除成功！");
			}else{
				$("#hintBody").text("删除失败！");
			}
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();	//重载当前页面
			}, 2000);
		}
	);
	//$(this).parents(".media").remove();
}