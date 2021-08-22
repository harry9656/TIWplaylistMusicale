function makePostCallWithForm(url, formElement, onSuccess, onFailure) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = () => {
        if (req.readyState === XMLHttpRequest.DONE) {
            let response = req.responseText;
            switch (req.status) {
                case 200:
                    onSuccess(response);
                    break;
                case 401: // unauthorized
                    window.location.href = "index.html";
                    break;
                case 400: // bad request
                case 500: // server error
                default:
                    onFailure(req.status);
            }
        }
    };
    req.open("POST", url);
    if (formElement == null) {
        req.send();
    } else if (formElement instanceof FormData) {
        req.send(formElement);
    } else {
        req.send(new FormData(formElement));
    }
}

function makeCall(method, url, onSuccess, onFailure) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = () => {
        if (req.readyState === XMLHttpRequest.DONE) {
            let response = req.responseText;
            switch (req.status) {
                case 200:
                    onSuccess(response);
                    break;
                case 401: // unauthorized
                    window.location.href = "index.html";
                    break;
                case 400: // bad request
                case 500: // server error
                default:
                    onFailure(req.status);
            }
        }
    };
    req.open(method, url);
    req.send();
}

function getSongsSortedByPlaylistData(songsMetaDataList, playlistSongsList) {
    const containsNegativeWeight = element => element.orderWeight === -1;
    if (playlistSongsList.every(containsNegativeWeight)) {
        let dateOrderedSongs = songsMetaDataList.sort((a, b) => {
            let keyA = new Date(a.publicationYear.year + "/" + a.publicationYear.month + "/" + a.publicationYear.day);
            let keyB = new Date(b.publicationYear.year + "/" + b.publicationYear.month + "/" + b.publicationYear.day);
            if (keyA < keyB) return 1;
            if (keyA === keyB) return 0;
            return -1;
        });
        dateOrderedSongs.forEach((value, index) => playlistSongsList.find(element => element.songId === value.songId).orderWeight = index);
        return dateOrderedSongs;
    } else {
        playlistSongsList.sort((a, b) => {
            if (a.orderWeight > b.orderWeight) return 1;
            if (a.orderWeight === b.orderWeight) return 0;
            return -1;
        });
        let weightOrderedSongs = playlistSongsList.map(element => songsMetaDataList.find(song => song.songId === element.songId));
        weightOrderedSongs.forEach((value, index) => playlistSongsList.find(element => element.songId === value.songId).orderWeight = index);
        return weightOrderedSongs;
    }
}