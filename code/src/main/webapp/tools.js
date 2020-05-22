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
	if(typeof form=="string") var formData=new FormData(document.querySelector(form)).entries();
	else if(form instanceof HTMLElement) var formData=new FormData(form);
	else {
		var formData = new FormData();
		for ( var key in form ) {
			formData.append(key, form[key]);
		}
	}
	return fetch(url+path,{
		method:'POST',
		body:new URLSearchParams(formData),
		headers: {
			'Authorization': 'Bearer ' + window.sessionStorage.getItem("jwt")||""
		}
	}).then(handleResponse);
}