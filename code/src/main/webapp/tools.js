function handleResponse(response) {
	return new Promise((resolve,reject)=>{
		if (response.status === 200) {
			resolve(response.json());
		} else {
			reject('Invalid JSON');
		}
	}).then(response=>{
		if(response.status=="OK") return response;
		else throw new Error(response.error)
	})
}

function sendPost(path,params) {
	return fetch(url+path,{
		method:'POST',
		credentials: 'include',
		body:JSON.stringify(params),
		headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
		}
	}).then(handleResponse)
}

function sendGet(path,params) {
	return fetch(url+path,{
		method:'GET',
		credentials: 'include',
		headers: {
			'Accept': 'application/json',
		}
	}).then(handleResponse)
}
