"""
API REST do SoundMatch (FastAPI).

Como rodar (dentro da pasta /backend):
    python -m venv .venv
    source .venv/bin/activate          # Windows: .venv\\Scripts\\activate
    pip install -r requirements.txt
    uvicorn main:app --host 0.0.0.0 --port 8000 --reload

Documentação Swagger: http://localhost:8000/docs
"""
from fastapi import Depends, FastAPI, HTTPException, Response, status
from sqlalchemy import func, select
from sqlalchemy.orm import Session, joinedload

import models
import schemas
from database import Base, engine, get_db

# Cria as tabelas users e recommendations no soundmatch.db (se ainda não existirem).
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="SoundMatch API",
    description="API REST do sistema de Perfil Sonoro e Recomendação Musical (AP2 Mobile).",
    version="1.0.0",
)


def _get_recommendation_or_404(db: Session, recommendation_id: int) -> models.Recommendation:
    """Busca uma recomendação (já carregando o usuário) ou retorna erro 404."""
    rec = db.scalar(
        select(models.Recommendation)
        .options(joinedload(models.Recommendation.user))
        .where(models.Recommendation.id == recommendation_id)
    )
    if rec is None:
        raise HTTPException(status_code=404, detail="Recomendação não encontrada")
    return rec


@app.get("/", tags=["Status"], summary="Verifica se a API está no ar")
def root():
    return {"status": "ok", "docs": "/docs"}


@app.post(
    "/recommendations",
    response_model=schemas.RecommendationOut,
    status_code=status.HTTP_201_CREATED,
    tags=["Recommendations"],
    summary="Salva uma recomendação (cria ou reaproveita o usuário)",
)
def create_recommendation(payload: schemas.RecommendationCreate, db: Session = Depends(get_db)):
    # 1) Procura um usuário com o mesmo nome (sem diferenciar maiúsculas/minúsculas).
    user = db.scalar(
        select(models.User).where(func.lower(models.User.name) == payload.name.strip().lower())
    )

    if user is None:
        # 2a) Não existe: cria um novo registro na tabela users.
        user = models.User(
            name=payload.name.strip(),
            favorite_artist=payload.favorite_artist,
            listening_mode=payload.listening_mode,
            plays_instrument=payload.plays_instrument,
        )
        db.add(user)
    else:
        # 2b) Já existe: associa a ele e atualiza o perfil com os dados mais recentes.
        user.favorite_artist = payload.favorite_artist
        user.listening_mode = payload.listening_mode
        user.plays_instrument = payload.plays_instrument

    # 3) Cria a recomendação ligada ao usuário (o SQLAlchemy preenche user_id sozinho).
    rec = models.Recommendation(
        user=user,
        score=payload.score,
        archetype=payload.archetype,
        recommended_song=payload.recommended_song,
        song_artist=payload.song_artist,
        subgenres=payload.subgenres,
        liked=payload.liked,
    )
    db.add(rec)
    db.commit()
    db.refresh(rec)
    return rec


@app.get(
    "/recommendations",
    response_model=list[schemas.RecommendationOut],
    tags=["Recommendations"],
    summary="Lista todas as recomendações com os dados do usuário",
)
def list_recommendations(db: Session = Depends(get_db)):
    return db.scalars(
        select(models.Recommendation)
        .options(joinedload(models.Recommendation.user))  # traz o usuário no mesmo SELECT (JOIN)
        .order_by(models.Recommendation.created_at.desc())
    ).all()


@app.get(
    "/recommendations/{recommendation_id}",
    response_model=schemas.RecommendationOut,
    tags=["Recommendations"],
    summary="Busca uma recomendação pelo id",
)
def get_recommendation(recommendation_id: int, db: Session = Depends(get_db)):
    return _get_recommendation_or_404(db, recommendation_id)


@app.put(
    "/recommendations/{recommendation_id}",
    response_model=schemas.RecommendationOut,
    tags=["Recommendations"],
    summary="Muda a avaliação (curti / não curti) de uma recomendação",
)
def update_recommendation(
    recommendation_id: int,
    payload: schemas.RecommendationUpdate,
    db: Session = Depends(get_db),
):
    rec = _get_recommendation_or_404(db, recommendation_id)
    rec.liked = payload.liked
    db.commit()
    db.refresh(rec)
    return rec


@app.delete(
    "/recommendations/{recommendation_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    tags=["Recommendations"],
    summary="Remove uma recomendação do banco",
)
def delete_recommendation(recommendation_id: int, db: Session = Depends(get_db)):
    rec = _get_recommendation_or_404(db, recommendation_id)
    db.delete(rec)
    db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)
