class Post {
	constructor(id,media,page,text,user,voteTotal,children,repliedTo) {
		this.id=id;
		this.media=media;
		this.page=page;
		this.text=text;
		this.user=user;
		this.voteTotal=voteTotal;
		this.children=children;
		this.repliedTo=repliedTo;
	}
	static getPost(id) {
		Utils.sendGet("/rest/post/"+id).then(Post.fromRaw)
	}
	static fromRaw(raw) {
		if(raw.children!==null) raw.children=raw.children.map(Post.fromRaw);
		else raw.children=[];
		raw.media=raw.media.map(Media.fromRaw);
		return new Post(raw.id,raw.media,raw.page,raw.text,raw.user,raw.voteTotal,raw.children,raw.repliedTo);
	}
}