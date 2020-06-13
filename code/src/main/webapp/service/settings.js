class Settings {
	constructor(name,email) {
		this.name=name;
		this.email=email;
	}
	static getSettings() {
		return Utils.sendGet('user/'+getLoggedInId()+'/settings').then(Settings.fromRaw);
	}
	static fromRaw(raw) {
		return new Settings(raw.name,raw.email);
	}
	save() {
		let formData = new FormData();
		formData.append("name",this.name);
		formData.append("email",this.email);
		return Utils.sendPost("user/"+getLoggedInId()+"/settings",formData);
	}
	static subscribe() {
		if(window.localStorage.getItem("long")!=="true") return Promise.reject("long");
		return Promise.all([Utils.sendGet('notification'),Settings.getSw()]).then(list=> {
			return list[1].pushManager.subscribe({
				userVisibleOnly: true,
				applicationServerKey: Settings.urlB64ToUint8Array(list[0].key)
			})
		}).then(subscription=>JSON.parse(JSON.stringify(subscription)))
		.then(subscription=>{
			return Utils.sendPost('notification',generateFormData({
				endpoint:subscription.endpoint,
				auth:subscription.keys.auth,
				key:subscription.keys.p256dh
			}))
		})
	}
	static urlB64ToUint8Array(base64String) {
		const padding = '='.repeat((4 - base64String.length % 4) % 4);
		const base64 = (base64String + padding)
			.replace(/\-/g, '+')
			.replace(/_/g, '/');

		const rawData = window.atob(base64);
		const outputArray = new Uint8Array(rawData.length);

		for (let i = 0; i < rawData.length; ++i) {
			outputArray[i] = rawData.charCodeAt(i);
		}
		return outputArray;
	}
	static unsubscribe() {
		return Settings.getSubscription().then(subscription=> {
			if(!subscription) return Promise.resolve();
			return Utils.sendDelete('notification/' + JSON.parse(JSON.stringify(subscription)).keys.auth)
				.catch(message => {
					if (message == 404) return
					else throw message
				}).then(() => subscription.unsubscribe());
		})
	}
	static getSw() {
		return navigator.serviceWorker.register('service-worker.js')
	}
	static getSubscription() {
		return Settings.getSw().then(sw=>sw.pushManager.getSubscription());
	}
}