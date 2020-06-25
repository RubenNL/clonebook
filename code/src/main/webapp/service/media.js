class Media {
	constructor(id) {
		this.id=id;
	}
	static fromRaw(raw) {
		return new Media(raw.id);
	}
	static create(file) {
		let data=new FormData();
		data.append('file',file);
		return Utils.sendFetch("media",{
			method:'POST',
			body:data
		}).then(Utils.handleResponse);
	}
	getUrl() {
		return '/rest/media/'+this.id;
	}
	static fromId(id) {
		return new Media(id);
	}
}