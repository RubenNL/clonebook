$('.asideTab').on('click',event=>{
	$('.asideBar').hide();
	$('.asideBar#'+$(event.currentTarget).attr('tab')).show();
})
function onLogin(user) {
	user.getPages().then(pages=>{
		pages.forEach(page=>{
			$('#pages').append('<a href="#page='+page.id+'">'+page.name+'</a><br>')
		})
	})
	user.getOwnPages().then(pages=>{
		pages.forEach(page=>{
			$('#myPages').append('<a href="#page='+page.id+'">'+page.name+'</a><br>')
		})
	})
}