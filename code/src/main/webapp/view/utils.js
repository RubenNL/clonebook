function generateFormData(form) {
	if(typeof form=="string") return new FormData(document.querySelector(form)).entries();
	if(form instanceof HTMLElement) return new FormData(form);
	else {
		let formData = new FormData();
		for ( const key in form ) {
			formData.append(key, form[key]);
		}
		return formData;
	}
}