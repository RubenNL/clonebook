$('#showNewAccount').on('click',()=>{
    $('#newAccountScreen').toggle()
    $('#loginScreen').toggle()
})
$('#createNewAccountButton').on("click",()=>{
    fetch(url+'/rest/register',{
        method:'POST',
        body:JSON.stringify({
            email:$('#newAccountEmail').val()
        }),
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    }).then(response=>{
        if (response.status === 200) {
            return response.json();
        } else {
            throw new Error('Something went wrong on api server!');
        }
    }).then(response=>{
        console.log(response);
        alert('Account aangevraagd. Kijk op je e-mail');
        $('#newAccountEmail').val('');
    }).catch(exception=>{
        alert(exception);
    })
})