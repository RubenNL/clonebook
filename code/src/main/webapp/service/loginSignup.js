class LoginSignup {
	static passwordRequest(form) {
		return Utils.sendPost("user/new",form);
	}
	static savePassword(form) {
		return Utils.sendPost("user/newPassword",form);
	}
	static login(form) {
		return Utils.sendPost("login",form).then(response=>{
			setJWT(response.JWT);
		})
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