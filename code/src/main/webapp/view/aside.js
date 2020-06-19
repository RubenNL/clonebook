$('.asideTab').on('click',event=>{
	$('.asideBar').hide();
	$('.asideBar#'+$(event.currentTarget).attr('tab')).show();
})