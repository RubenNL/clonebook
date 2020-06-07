class LoginSignup {
	static passwordRequest(form) {
		return Utils.sendPost("/rest/user/new",form);
	}
	static savePassword(form) {
		return Utils.sendPost("/rest/user/newPassword",form);
	}
	static login(form) {
		return Utils.sendPost("/rest/login",form).then(response=>{
			window.sessionStorage.setItem("jwt",response.JWT);
		})
	}
	static logout() {
		window.sessionStorage.removeItem("jwt");
		return Promise.resolve();
	}
	static getLoggedinUser() {
		return new Promise((resolve,reject)=>{
			const token=sessionStorage.getItem("jwt");
			if(token.length===0) {
				reject("niet ingelogd");
				return;
			}
			const base64Url = token.split('.')[1];
			const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
			const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
				return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
			}).join(''));
			resolve(JSON.parse(jsonPayload).sub);
		}).then(User.getUser)
	}
}