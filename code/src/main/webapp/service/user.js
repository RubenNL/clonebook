class User {
	constructor(id,name,email,privatePageId,profilePicture) {
		this.id=id;
		this.name=name;
		this.email=email;
		this.privatePageId=privatePageId;
		this.profilePicture=profilePicture;
	}
	static getUser(id) {
		return Utils.sendGet("user/"+id).then(User.fromRaw);
	}
	static fromRaw(raw) {
		return new User(raw.id,raw.name,raw.email,raw.privatePageId,raw.profilePicture);
	}
	getPage() {
		return Page.getPage(this.privatePageId);
	}
	getLidAanvragenOpPaginas() {
		return Utils.sendGet('user/'+this.id+'/lidAanvragen');
	}

}