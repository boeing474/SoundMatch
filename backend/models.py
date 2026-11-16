"""
Modelos ORM: cada classe representa uma tabela do banco relacional.

Relacionamento: 1 User  --->  N Recommendations
(um usuário pode ter várias recomendações; cada recomendação pertence a um usuário)
"""
from datetime import datetime, timezone

from sqlalchemy import Boolean, DateTime, ForeignKey, Integer, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from database import Base


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


class User(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    name: Mapped[str] = mapped_column(String(100), nullable=False, index=True)
    favorite_artist: Mapped[str] = mapped_column(String(100), nullable=False)
    listening_mode: Mapped[str] = mapped_column(String(50), nullable=False)
    plays_instrument: Mapped[bool] = mapped_column(Boolean, default=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)

    # Lado "1" da relação: acessa todas as recomendações do usuário (user.recommendations).
    # cascade="all, delete-orphan": se o usuário for apagado, suas recomendações também são.
    recommendations: Mapped[list["Recommendation"]] = relationship(
        back_populates="user", cascade="all, delete-orphan"
    )


class Recommendation(Base):
    __tablename__ = "recommendations"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)

    # Chave estrangeira: cada recomendação pertence a um usuário
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"), nullable=False, index=True)

    score: Mapped[int] = mapped_column(Integer, nullable=False)  # % de match (10 a 100)
    archetype: Mapped[str] = mapped_column(String(100), nullable=False)  # perfil, ex.: "Pop & K-pop"
    recommended_song: Mapped[str] = mapped_column(String(150), nullable=False)  # ex.: "Espresso"
    song_artist: Mapped[str] = mapped_column(String(100), nullable=False)       # ex.: "Sabrina Carpenter"
    subgenres: Mapped[str] = mapped_column(String(200), default="")  # outros estilos, ex.: "Eletrônica"
    liked: Mapped[bool] = mapped_column(Boolean, nullable=False)  # Curti / Não curti
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)

    # Lado "N" da relação: acessa o usuário dono da recomendação (recommendation.user).
    user: Mapped["User"] = relationship(back_populates="recommendations")
