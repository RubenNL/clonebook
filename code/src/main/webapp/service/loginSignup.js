class LoginSignup {
	static passwordRequest(form) {
		return Utils.sendPost("register",form);
	}
	static savePassword(form) {
		return Utils.sendPost("user/newPassword",form);
	}
	static login(form) {
		let next=form.next();
		let data={};
		while(!next.done) {
			data[next.value[0]]=next.value[1];
			next=form.next();
		}
		return Utils.sendFetch("login",{
			method:'POST',
			headers: {'Content-Type':"application/json;charset=UTF-8"},
			//body:new URLSearchParams(formData)
			body:JSON.stringify(data)
		}).then(response=>{
			debugger;
			setJWT(response.headers.get('Authorization').split(' ')[1]);
		});
	}
	static logout() {
		removeJWT();
		return caches.keys().then(function(names) {
			return Promise.all(names.map(name=>{
				return caches.delete(name);
			}));
		});
	}
	static getLoggedinUser() {
		const id=getLoggedInId();
		if(id==null) return Promise.reject("niet ingelogd");
		else return User.getUser(id);
	}
}