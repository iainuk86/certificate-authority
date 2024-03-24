function getSelectedClientCertificate() {
    let selectedCert = document.querySelector( 'input[name="client-cert-radio"]:checked');

    if (selectedCert == null) {
        alert("Please select a Client Certificate");
        return;
    }

    let secretUrl = '/api/secret/' + selectedCert.value
    fetch(secretUrl).then(resp => console.log(resp.text()))
}