class Media {
	constructor(id,mime,ownerId) {
		this.id=id;
		this.mime=mime;
		this.ownerId=ownerId;
	}
	getOwner() {
		return User.getUser(this.ownerId);
	}
	static fromRaw(raw) {
		return new Media(raw.id,raw.mime,raw.ownerId);
	}
	static create(file) {
		let data=new FormData();
		data.append('file',file);
		return Utils.sendFetch("/rest/media",{
			method:'POST',
			body:data,
			headers: {
				'Authorization': 'Bearer ' + window.sessionStorage.getItem("jwt")||""
			}
		}).then(Utils.handleResponse);
	}
	getUrl() {
		return '/rest/media/'+this.id;
	}
}