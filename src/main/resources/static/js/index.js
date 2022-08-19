$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	// 获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	// 发送异步请求（POST）
	$.post(
	//	访问路径
		CONTEXT_PATH + "/discuss/add",
		{"title":title, "content":content},
		function (data) {
			data = $.parseJSON(data);
			// 在提示框中显示返回消息
			$("#hintBody").text(data.msg);
			//显示提示框
			$("#hintModal").modal("show");
			// 2秒后隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				// 成功则刷新页面
				if (data.code == 0) {
					window.location.reload();
				}
			}, 2000);
		}
	)


}