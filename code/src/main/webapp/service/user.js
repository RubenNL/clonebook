class User {
	constructor(id,name,email,privatePageId) {
		this.id=id;
		this.name=name;
		this.email=email;
		this.privatePageId=privatePageId;
	}
	static getUser(id) {
		return Utils.sendGet("/rest/user/"+id).then(User.fromRaw);
	}
	static fromRaw(raw) {
		return new User(raw.id,raw.name,raw.email,raw.privatePageId);
	}
	getPage() {
		return Page.getPage(this.privatePageId);
	}

}