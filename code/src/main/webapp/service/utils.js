class Utils {
	static sendFetch(first,second) {
		if(!second.headers) second.headers={};
		second.headers.Authorization='Bearer ' + getJWT();
		//if(navigator.onLine) return fetch('/rest/'+first,second);
		if(navigator.onLine) return fetch(first,second);
		alert("apparaat is offline! maak verbinding met het internet en probeer opnieuw.");
		return Promise.reject("offline");
	}
	static handleResponse(response) {
		return new Promise(async (resolve,reject)=>{
			const contentType = response.headers.get("content-type");
			if (contentType && contentType.indexOf("application/json") !== -1) {
				if(response.ok) resolve(await response.json());
				else reject(await response.json());
			} else if(response.ok) resolve(response.text());
			else reject(response.status);
		})
	}
	static sendPost(path,formData) {
		let next=formData.next();
		let data={};
		while(!next.done) {
			data[next.value[0]]=next.value[1];
			next=formData.next();
		}
		return Utils.sendFetch(path,{
			method:'POST',
			headers: {'Content-Type':"application/json;charset=UTF-8"},
			//body:new URLSearchParams(formData)
			body:JSON.stringify(data)
		}).then(Utils.handleResponse);
	}
	static sendGet(path) {
		return Utils.sendFetch(path,{
			method:'GET'
		}).then(Utils.handleResponse);
	}
	static sendDelete(path) {
		return Utils.sendFetch(path,{
			method:'DELETE'
		}).then(Utils.handleResponse)
	}
}