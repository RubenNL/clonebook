function sendPost(path,params) {
	return fetch(url+path,{
		method:'POST',
		credentials: 'include',
		body:JSON.stringify(params),
		headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
		}
	}).then(response=>{
		if (response.status === 200) {
			return response.json();
		} else {
			throw new Error('Something went wrong on api server!');
		}
	})
}
function sendGet(path,params) {
	return fetch(url+path,{
		method:'GET',
		credentials: 'include',
		headers: {
			'Accept': 'application/json',
		}
	}).then(response=>{
		if (response.status === 200) {
			return response.json();
		} else {
			throw new Error('Something went wrong on api server!');
		}
	})
}
