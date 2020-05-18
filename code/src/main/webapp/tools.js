function handleResponse(response) {
	console.log(response);
	if(response.ok) {
		const contentType = response.headers.get("content-type");
		if (contentType && contentType.indexOf("application/json") !== -1) return response.json();
		else return response.text();
	} else throw response.status;
}
function sendGet(path) {
	return fetch(url+path,{
		method:'GET',
		headers: {
			'Authorization': 'Bearer ' + window.sessionStorage.getItem("jwt")||""
		}
	}).then(handleResponse);
}
function sendPost(path,form) {
	return fetch(url+path,{
		method:'POST',
		body:new URLSearchParams(new FormData(document.querySelector(form)).entries()),
		headers: {
			'Authorization': 'Bearer ' + window.sessionStorage.getItem("jwt")||""
		}
	}).then(handleResponse);
}