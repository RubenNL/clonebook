function getStorage() {
	if(window.localStorage.getItem("long")==="true") return window.localStorage;
	else return window.sessionStorage;
}
function getLoggedInId() {
	const parsed=parseJWT(getJWT());
	if(parsed==null) return null;
	else return parsed.sub;
}
function parseJWT(token) {
	if(!token || token.length==0) return null;
	const base64Url = token.split('.')[1];
	const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
	const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
		return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
	}).join(''));
	return JSON.parse(jsonPayload)
}
function getJWT() {
	return getStorage().getItem("jwt")||"";
}
function setJWT(jwt) {
	window.localStorage.setItem("long",parseJWT(jwt).long.toString());
	return getStorage().setItem("jwt",jwt);
}
function removeJWT() {
	window.sessionStorage.clear();
	window.localStorage.clear();
}