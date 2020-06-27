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
		if(this.id) return '/rest/media/'+this.id;
		else return 'icon.svg';
	}
	static fromId(id) {
		return new Media(id);
	}
}