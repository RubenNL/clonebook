function addPost(imgLocation,name,date,text) {
	let node = $('#postTemplate').contents("article").clone();
	node.find('.profilePicture').attr('img',imgLocation);
	node.find('.profilePicture').attr('alt',name);
	node.find('.name').text(name);
	node.find('.date').text(date);
	node.find('.text').text(text);
	$('#posts').append(node);
}