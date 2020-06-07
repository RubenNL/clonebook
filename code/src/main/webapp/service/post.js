class Post {
	constructor(id,media,pageId,text,user,voteTotal,children,repliedTo) {
		this.id=id;
		this.media=media;
		this.pageId=pageId;
		this.text=text;
		this.user=user;
		this.voteTotal=voteTotal;
		this.children=children;
		this.repliedTo=repliedTo;
	}
	static getPost(id) {
		return Utils.sendGet("post/"+id).then(Post.fromRaw)
	}
	static fromRaw(raw) {
		if(raw.children!==null) raw.children=raw.children.map(Post.fromRaw);
		else raw.children=[];
		raw.media=raw.media.map(Media.fromRaw);
		return new Post(raw.id,raw.media,raw.pageId,raw.text,raw.user,raw.voteTotal,raw.children,raw.repliedTo);
	}
	getPage() {
		return Page.getPage(this.pageId);
	}
}