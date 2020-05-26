class Page {
	constructor(id,last10Posts,logo,name,owner) {
		this.id=id;
		this.last10Posts=last10Posts;
		this.logo=logo;
		this.name=name;
		this.owner=owner;
	}
	static getPage(id) {
		return Utils.sendGet("/rest/page/"+id).then(Page.fromRaw);
	}
	static fromRaw(raw) {
		raw.last10Posts=raw.last10Posts.map(sourcePost=>Post.fromRaw(sourcePost));
		return new Page(raw.id,raw.last10Posts,raw.logo,raw.name,raw.owner);
	}
}