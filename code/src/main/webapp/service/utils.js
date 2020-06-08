class Utils {
	static sendFetch(first,second) {
		if(!second.headers) second.headers={};
		second.headers.Authorization='Bearer ' + window.sessionStorage.getItem("jwt")||"";
		if(navigator.onLine) return fetch('/rest/'+first,second);
		alert("apparaat is offline! maak verbinding met het internet en probeer opnieuw.");
		return Promise.reject("offline");
	}
	static handleResponse(response) {
		return new Promise(async (resolve,reject)=>{
			console.log(response);
			const contentType = response.headers.get("content-type");
			if (contentType && contentType.indexOf("application/json") !== -1) {
				if(response.ok) resolve(await response.json());
				else reject(await response.json());
			} else if(response.ok) resolve();
			else reject(response.status);
		})
	}
	static sendPost(path,formData) {
		return Utils.sendFetch(path,{
			method:'POST',
			body:new URLSearchParams(formData)
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