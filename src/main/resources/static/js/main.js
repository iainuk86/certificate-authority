const RESULT = {
    SUCCESS: 'success',
    FAILURE: 'failure'
}

function fetchWithSelectedCertificate() {
    hidePreviousResult();

    let selectedCert = document.querySelector( 'input[name="client-cert-radio"]:checked');

    if (selectedCert == null) {
        alert('Please select a Client Certificate');
        return;
    }

    fetchSecret(selectedCert)
        .then(response => displayResult(response))
}

async function fetchSecret(selectedCert) {
    return await fetch('/api/secret/' + selectedCert.value)
        .then(response => {
            if (response.status !== 200) {
                throw new Error('Request failed!')
            }

            // The server will respond with a String response body if the call is successful
            return response.text()
        }).then(secret => {
            return {
                result: RESULT.SUCCESS,
                message: secret,
                color: '#378a37'
            }
        }).catch((err) => {
            console.log(err)
            return {
                result: RESULT.FAILURE,
                message: 'Denied! Try again',
                color: 'red'
            }
        })
}

function displayResult(response) {
    let secret = document.getElementById('secret')
    secret.textContent = response.message
    secret.style.color = response.color

    let anim;
    if (response.result === RESULT.SUCCESS) {
        anim = document.getElementById('success-anim')
    } else {
        anim = document.getElementById('failure-anim')
    }

    secret.style.visibility = 'visible'
    anim.style.display = 'block'
    anim.play()
}

function hidePreviousResult() {
    document.getElementById('secret').style.visibility = 'hidden'

    let anim = document.getElementById('success-anim')
    anim.style.display = 'none'
    anim.stop()

    anim = document.getElementById('failure-anim')
    anim.style.display = 'none'
    anim.stop()
}