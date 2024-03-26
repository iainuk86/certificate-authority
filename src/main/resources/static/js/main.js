function fetchWithSelectedCertificate() {
    let selectedCert = document.querySelector( 'input[name="client-cert-radio"]:checked');

    if (selectedCert == null) {
        alert("Please select a Client Certificate");
        return;
    }

    fetchSecret(selectedCert)
        .then(result => displayResult(result))
}

async function fetchSecret(selectedCert) {
    return await fetch('/api/secret/' + selectedCert.value)
        .then(response => {
            if (response.status !== 200) {
                throw new Error("Request failed!")
            }

            // The server will respond with a String response body if the call is successful
            return response.text()
        }).then(secret => {
            return {
                message: secret,
                color: "green"
            }
        }).catch((err) => {
            console.log(err)
            return {
                message: "Denied! Try again",
                color: "red"
            }
        })
}

function displayResult(result) {
    let secret = document.getElementById('secret')
    secret.textContent = result.message
    secret.style.color = result.color
    secret.style.display = "block"
}