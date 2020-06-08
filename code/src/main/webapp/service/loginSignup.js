class LoginSignup {
	static passwordRequest(form) {
		return Utils.sendPost("new",form);
	}
	static savePassword(form) {
		return Utils.sendPost("newPassword",form);
	}
	static login(form) {
		return Utils.sendPost("login",form).then(response=>{
			window.sessionStorage.setItem("jwt",response.JWT);
		})
	}
	static logout() {
		window.sessionStorage.removeItem("jwt");
		return Promise.resolve();
	}
	static getLoggedinUser() {
		const id=getLoggedInId();
		if(id==null) return Promise.reject("niet ingelogd");
		else return User.getUser(id);
	}
}