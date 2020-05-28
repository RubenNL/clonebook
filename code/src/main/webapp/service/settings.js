class Settings {
	constructor(name,email) {
		this.name=name;
		this.email=email;
	}
	static getSettings() {
		return Utils.sendGet('/rest/user/settings').then(Settings.fromRaw);
	}
	static fromRaw(raw) {
		return new Settings(raw.name,raw.email);
	}
	save() {
		let formData = new FormData();
		formData.append("name",this.name);
		formData.append("email",this.email);
		return Utils.sendPost("/rest/user/settings",formData);
	}
}