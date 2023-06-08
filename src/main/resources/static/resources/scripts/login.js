const loginForm = document.getElementById('loginForm');

loginForm.onsubmit = e => {
    e.preventDefault();

    if (loginForm['email'].value === '') {
        dialogCover.show();
        dialogLayer.show({
            title: '로그인',
            content: '이메일을 입력해 주세요.',
            onConfirm: e => {
                e.preventDefault();
                dialogCover.hide();
                dialogLayer.hide();
                loginForm['email'].focus();
            }
        });
        return;
    }
    if (loginForm['password'].value === '') {
        dialogCover.show();
        dialogLayer.show({
            title: '로그인',
            content: '비밀번호를 입력해 주세요.',
            onConfirm: e => {
                e.preventDefault();
                dialogCover.hide();
                dialogLayer.hide();
                loginForm['password'].focus();
            }
        });
        return;
    }
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', loginForm['email'].value);
    formData.append('password', loginForm['password'].value);
    xhr.open('POST', './login');
    xhr.onreadystatechange = () => {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status >= 200 && xhr.status < 300) {
                const responseObject = JSON.parse(xhr.responseText);
                switch (responseObject.result) {
                    case 'success':
                        location.href += '';
                        break;
                    case 'failure':
                    default:
                        dialogCover.show();
                        dialogLayer.show({
                            title: '로그인',
                            content: '이메일 혹은 비밀번호가 틀렸습니다.<br><br>다시 확인해 주세요.',
                            onConfirm: e => {
                                e.preventDefault();
                                dialogCover.hide();
                                dialogLayer.hide();
                                loginForm['email'].focus();
                                loginForm['email'].select();
                            }
                        });
                }
            } else {
                dialogCover.show();
                dialogLayer.show({
                    title: '통신 오류',
                    content: '서버와 통신하지 못하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                    onConfirm: e => {
                        e.preventDefault();
                        dialogCover.hide();
                        dialogLayer.hide();
                    }
                });
            }
        }
    };
    xhr.send(formData);
};














